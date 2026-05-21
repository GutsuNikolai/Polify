import { http } from "./http";
import { LedgerEntryResponse } from "../types/ledger";

export async function listLedgerEntries(attemptId?: number): Promise<LedgerEntryResponse[]> {
  const qs = attemptId != null ? `?attemptId=${attemptId}` : "";
  const resp = await http.get<LedgerEntryResponse[]>(`/ledger${qs}`);
  return resp.data;
}

