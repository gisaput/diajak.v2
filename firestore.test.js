const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_participant_1";
const BOB_UID = "bob_organizer_2";
const CHARLIE_UID = "charlie_outsider_3";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

function futureDate(offsetMs = 60000) {
  return new Date(Date.now() + offsetMs);
}

function buildUserProfile(uid, overrides = {}) {
  return {
    userId: uid,
    displayName: "Pengguna Diajak",
    bio: "Suka lari dan padel",
    city: "Jakarta Selatan",
    photoUrl: "",
    interests: ["Lari", "Padel"],
    organizerVerified: false,
    kycStatus: "UNVERIFIED",
    createdAt: pastDate(10000),
    updatedAt: pastDate(10000),
    ...overrides,
  };
}

function buildActivity(organizerId, overrides = {}) {
  return {
    organizerId,
    organizerName: "Komunitas Lari JKT",
    organizerAvatarKey: "img_activity_running",
    organizerVerified: false,
    title: "Sunday Morning 10K Run",
    description: "Lari santai bersama komunitas di area GBK Senayan dengan pacer berpengalaman.",
    category: "Lari",
    categoryEmoji: "🏃",
    city: "Jakarta Selatan",
    locationName: "GBK Senayan, Jakarta",
    latitude: -6.2186,
    longitude: 106.8025,
    eventDateMillis: Date.now() + 86400000 * 5,
    timeLabel: "06:30 WIB",
    priceIdr: 75000,
    quota: 50,
    joinedCount: 0,
    favoriteCount: 0,
    viralScore: 10,
    photoKeys: ["img_activity_running"],
    benefits: ["Air Mineral", "Dokumentasi Foto"],
    joinedUserNames: [],
    status: "DRAFT",
    averageRating: 0,
    reviewCount: 0,
    seededSample: false,
    createdAt: pastDate(10000),
    updatedAt: pastDate(10000),
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Guest can read PUBLISHED activities & reviews only, denied drafts, writes, and private collections", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("activities").doc("pub_1").set(
      buildActivity(BOB_UID, { status: "PUBLISHED", organizerVerified: true })
    );
    await db.collection("activities").doc("draft_1").set(
      buildActivity(BOB_UID, { status: "DRAFT" })
    );
    await db.collection("activities").doc("pub_1").collection("reviews").doc("rev_pub").set({
      activityId: "pub_1",
      organizerId: BOB_UID,
      authorId: ALICE_UID,
      authorName: "Alice",
      rating: 5,
      comment: "Keren banget acaranya!",
      organizerReply: "",
      createdAt: pastDate(2000),
      updatedAt: pastDate(2000),
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  // Guest CAN browse published marketplace catalog and public reviews
  await assertSucceeds(unauthDb.collection("activities").where("status", "==", "PUBLISHED").get());
  await assertSucceeds(unauthDb.collection("activities").doc("pub_1").get());
  await assertSucceeds(
    unauthDb.collection("activities").doc("pub_1").collection("reviews").where("activityId", "==", "pub_1").get()
  );

  // Guest CANNOT read drafts, unfiltered activities, or write anything
  await assertFails(unauthDb.collection("activities").doc("draft_1").get());
  await assertFails(unauthDb.collection("activities").get());
  await assertFails(unauthDb.collection("activities").doc("guest_act").set(buildActivity(ALICE_UID)));

  // Guest CANNOT access any private user collections
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("bookings").where("userId", "==", ALICE_UID).get());
  await assertFails(unauthDb.collection("conversations").where("participantUids", "array-contains", ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("favorites").get());
});

test("User profile isolation, self-verification block, and atomic KYC verification gate", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Cannot create profile already verified
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set(buildUserProfile(BOB_UID, { organizerVerified: true, kycStatus: "VERIFIED" }))
  );

  // Valid initial profile creation
  await assertSucceeds(bobDb.collection("users").doc(BOB_UID).set(buildUserProfile(BOB_UID)));

  // Cross-user profile read denied
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());

  // Cannot mark verified without KTP document
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).update({
      organizerVerified: true,
      kycStatus: "VERIFIED",
      updatedAt: pastDate(1000),
    })
  );

  // Invalid NIK (not 16 digits) rejected
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).collection("kycDocuments").doc("ktp").set({
      userId: BOB_UID,
      fullName: "Bob Penyelenggara",
      nikNumber: "12345",
      documentPhotoUri: "content://picker/ktp.jpg",
      status: "VERIFIED",
      submittedAt: pastDate(1000),
    })
  );

  // Valid atomic batch: write KTP document + update profile to VERIFIED
  const batch = bobDb.batch();
  batch.set(bobDb.collection("users").doc(BOB_UID).collection("kycDocuments").doc("ktp"), {
    userId: BOB_UID,
    fullName: "Bob Penyelenggara",
    nikNumber: "3174012345678901",
    documentPhotoUri: "content://picker/ktp.jpg",
    status: "VERIFIED",
    submittedAt: pastDate(1000),
  });
  batch.update(bobDb.collection("users").doc(BOB_UID), {
    organizerVerified: true,
    kycStatus: "VERIFIED",
    updatedAt: pastDate(1000),
  });
  await assertSucceeds(batch.commit());

  // Cross-user KYC read denied
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("kycDocuments").doc("ktp").get()
  );
});

test("Organizer KYC gate blocks unverified publish, allows draft, allows publish once verified", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(bobDb.collection("users").doc(BOB_UID).set(buildUserProfile(BOB_UID)));

  // Unverified organizer CAN save DRAFT
  await assertSucceeds(
    bobDb.collection("activities").doc("act_1").set(buildActivity(BOB_UID, { status: "DRAFT" }))
  );

  // Unverified organizer CANNOT publish directly
  await assertFails(
    bobDb.collection("activities").doc("act_2").set(buildActivity(BOB_UID, { status: "PUBLISHED", organizerVerified: true }))
  );

  // Outsider CANNOT read Bob's DRAFT activity
  await assertFails(aliceDb.collection("activities").doc("act_1").get());

  // Unfiltered activities list query fails (Query Trust Test)
  await assertFails(aliceDb.collection("activities").get());

  // Verify Bob's KTP atomically
  const batch = bobDb.batch();
  batch.set(bobDb.collection("users").doc(BOB_UID).collection("kycDocuments").doc("ktp"), {
    userId: BOB_UID,
    fullName: "Bob Organizer",
    nikNumber: "3174012345678901",
    documentPhotoUri: "content://picker/ktp.jpg",
    status: "VERIFIED",
    submittedAt: pastDate(1000),
  });
  batch.update(bobDb.collection("users").doc(BOB_UID), {
    organizerVerified: true,
    kycStatus: "VERIFIED",
    updatedAt: pastDate(1000),
  });
  await assertSucceeds(batch.commit());

  // Verified organizer CAN publish activity
  await assertSucceeds(
    bobDb.collection("activities").doc("act_1").update({
      status: "PUBLISHED",
      organizerVerified: true,
      updatedAt: pastDate(1000),
    })
  );

  // Filtered published feed query succeeds for Alice
  await assertSucceeds(aliceDb.collection("activities").where("status", "==", "PUBLISHED").get());
  // Organizer query for own activities succeeds for Bob
  await assertSucceeds(bobDb.collection("activities").where("organizerId", "==", BOB_UID).get());
});

test("Booking RSVP, organizer check-in, wallet income ledger, reviews, and conversations", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).set(
      buildUserProfile(BOB_UID, { organizerVerified: true, kycStatus: "VERIFIED" })
    );
    await db.collection("activities").doc("sample_padel_jkt").set(
      buildActivity(BOB_UID, { status: "PUBLISHED", organizerVerified: true, seededSample: true })
    );
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  // Alice creates booking
  await assertSucceeds(
    aliceDb.collection("bookings").doc("book_1").set({
      activityId: "sample_padel_jkt",
      activityTitle: "Sunday Morning 10K Run",
      activityPhotoKey: "img_activity_running",
      activityLocation: "GBK Senayan, Jakarta",
      activityDateMillis: Date.now() + 86400000 * 5,
      activityTimeLabel: "06:30 WIB",
      userId: ALICE_UID,
      userName: "Alice",
      organizerId: BOB_UID,
      organizerName: "Komunitas Lari JKT",
      ticketCount: 1,
      totalPriceIdr: 75000,
      paymentMethod: "QRIS",
      ticketCode: "DJK-88219",
      status: "CONFIRMED",
      createdAt: pastDate(5000),
      updatedAt: pastDate(5000),
    })
  );

  // Charlie cannot read Alice's booking
  await assertFails(charlieDb.collection("bookings").doc("book_1").get());
  // Alice & Bob can query their scoped bookings
  await assertSucceeds(aliceDb.collection("bookings").where("userId", "==", ALICE_UID).get());
  await assertSucceeds(bobDb.collection("bookings").where("organizerId", "==", BOB_UID).get());

  // Bob checks in Alice's ticket
  await assertSucceeds(
    bobDb.collection("bookings").doc("book_1").update({
      status: "CHECKED_IN",
      updatedAt: pastDate(1000),
    })
  );
  // Terminal state lock: cannot update CHECKED_IN ticket back to CANCELLED
  await assertFails(
    aliceDb.collection("bookings").doc("book_1").update({
      status: "CANCELLED",
      updatedAt: pastDate(500),
    })
  );

  // Wallet income recorded for organizer on paid booking; Charlie cannot withdraw Bob's wallet
  await assertSucceeds(
    aliceDb.collection("users").doc(BOB_UID).collection("walletTransactions").doc("tx_1").set({
      userId: BOB_UID,
      type: "INCOME",
      amount: 75000,
      title: "Tiket Sunday Morning 10K Run (Alice)",
      referenceId: "book_1",
      status: "SUCCESS",
      createdAt: pastDate(1000),
    })
  );
  await assertFails(
    charlieDb.collection("users").doc(BOB_UID).collection("walletTransactions").doc("tx_steal").set({
      userId: BOB_UID,
      type: "WITHDRAWAL",
      amount: 75000,
      title: "Steal",
      referenceId: "steal",
      status: "PROCESSING",
      createdAt: pastDate(1000),
    })
  );

  // Review creation by Alice + Reply by Bob
  await assertSucceeds(
    aliceDb.collection("activities").doc("sample_padel_jkt").collection("reviews").doc("rev_1").set({
      activityId: "sample_padel_jkt",
      organizerId: BOB_UID,
      authorId: ALICE_UID,
      authorName: "Alice",
      rating: 5,
      comment: "Seru banget acaranya, rapi dan ramah!",
      organizerReply: "",
      createdAt: pastDate(3000),
      updatedAt: pastDate(3000),
    })
  );
  await assertSucceeds(
    bobDb.collection("activities").doc("sample_padel_jkt").collection("reviews").doc("rev_1").update({
      organizerReply: "Terima kasih Alice, sampai jumpa di sesi berikutnya!",
      updatedAt: pastDate(1000),
    })
  );

  // Direct conversation & message between Alice and Bob; Charlie blocked
  await assertSucceeds(
    aliceDb.collection("conversations").doc("direct_alice_bob").set({
      type: "DIRECT",
      activityId: "sample_padel_jkt",
      activityTitle: "Sunday Morning 10K Run",
      title: "Komunitas Lari JKT",
      organizerId: BOB_UID,
      participantUids: [ALICE_UID, BOB_UID],
      lastMessage: "Halo kak, titik kumpul dimana?",
      lastMessageSenderName: "Alice",
      createdAt: pastDate(2000),
      updatedAt: pastDate(2000),
    })
  );
  await assertFails(charlieDb.collection("conversations").doc("direct_alice_bob").get());
  await assertSucceeds(
    aliceDb.collection("conversations").doc("direct_alice_bob").collection("messages").doc("msg_1").set({
      conversationId: "direct_alice_bob",
      senderId: ALICE_UID,
      senderName: "Alice",
      text: "Halo kak, titik kumpul dimana?",
      participantUids: [ALICE_UID, BOB_UID],
      createdAt: pastDate(1000),
    })
  );
  await assertFails(
    charlieDb.collection("conversations").doc("direct_alice_bob").collection("messages").doc("msg_1").get()
  );

  // Tampered future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("sample_padel_jkt").set({
      activityId: "sample_padel_jkt",
      savedAt: futureDate(60000),
    })
  );
});

test("Group chat existence check before create, first participant create, and second participant join", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const groupRefAlice = aliceDb.collection("conversations").doc("group_sample_running_gbk");
  const groupRefCharlie = charlieDb.collection("conversations").doc("group_sample_running_gbk");

  // 1. Checking non-existent group chat returns exists=false instead of PERMISSION_DENIED
  const initialSnap = await assertSucceeds(groupRefAlice.get());
  if (initialSnap.exists) {
    throw new Error("Expected group_sample_running_gbk to not exist yet");
  }

  // 2. First participant creates group conversation & posts welcome message
  await assertSucceeds(
    groupRefAlice.set({
      type: "GROUP",
      activityId: "sample_running_gbk",
      activityTitle: "Sunday Morning Social 5K & 10K Run",
      title: "Grup: Sunday Morning Social 5K & 10K Run",
      organizerId: "community_senayan_run",
      participantUids: [ALICE_UID, "community_senayan_run"],
      lastMessage: "Alice bergabung ke aktivitas Sunday Morning Social 5K & 10K Run",
      lastMessageSenderName: "Alice",
      createdAt: pastDate(5000),
      updatedAt: pastDate(5000),
    })
  );
  await assertSucceeds(
    groupRefAlice.collection("messages").doc("msg_welcome_alice").set({
      conversationId: "group_sample_running_gbk",
      senderId: ALICE_UID,
      senderName: "Alice",
      text: "Halo semuanya! Saya baru saja memesan tiket.",
      participantUids: [ALICE_UID, "community_senayan_run"],
      createdAt: pastDate(4000),
    })
  );

  // 3. Second participant cannot read existing group chat before joining
  await assertFails(groupRefCharlie.get());

  // 4. Second participant joins existing GROUP chat via update adding themselves to participantUids
  await assertSucceeds(
    groupRefCharlie.update({
      participantUids: [ALICE_UID, "community_senayan_run", CHARLIE_UID],
      lastMessage: "Charlie bergabung ke aktivitas Sunday Morning Social 5K & 10K Run",
      lastMessageSenderName: "Charlie",
      updatedAt: pastDate(2000),
    })
  );

  // 5. After joining, Charlie can read the group chat and post messages
  await assertSucceeds(groupRefCharlie.get());
  await assertSucceeds(
    groupRefCharlie.collection("messages").doc("msg_welcome_charlie").set({
      conversationId: "group_sample_running_gbk",
      senderId: CHARLIE_UID,
      senderName: "Charlie",
      text: "Halo semuanya! Saya baru saja bergabung.",
      participantUids: [ALICE_UID, "community_senayan_run", CHARLIE_UID],
      createdAt: pastDate(1000),
    })
  );
});
