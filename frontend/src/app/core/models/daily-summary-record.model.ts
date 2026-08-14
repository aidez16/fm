/** Mirrors com.demo.futuremovement.dto.DailySummaryRecord on the backend. */
export interface DailySummaryRecord {
  clientInformation: string;
  productInformation: string;
  /** QUANTITY_LONG - QUANTITY_SHORT. Can be negative. */
  totalTransactionAmount: number;
}
