export interface ChartData {
  label: string;
  value: number;
}

export interface DashboardSummary {
  totalRevenue: number;
  totalInvoices: number;
  todayRevenue: number;
  growthRate: number;
  revenueOverTime: ChartData[];
  revenueStructure: ChartData[];
  topServices: ChartData[];
  topDoctors: ChartData[];
}
