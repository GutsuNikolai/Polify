export type LedgerEntryResponse = {
  id: number;
  attemptId: number;
  userId: number;
  amountBani: number;
  currency: "MDL";
  status: "CREATED" | "CONFIRMED" | "FAILED";
  createdAt: string;
};

