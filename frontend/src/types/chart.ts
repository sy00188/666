// 图表相关类型定义

export interface ChartDataPoint {
  name: string;
  value: number;
  category?: string;
  date?: string;
  [key: string]: any;
}

export interface ChartSeries {
  name: string;
  data: ChartDataPoint[];
  type?: "line" | "bar" | "pie" | "scatter" | "area";
  color?: string;
}

export interface ChartConfig {
  title?: string;
  subtitle?: string;
  xAxis?: {
    title?: string;
    categories?: string[];
    type?: "category" | "datetime" | "numeric";
  };
  yAxis?: {
    title?: string;
    min?: number;
    max?: number;
  };
  legend?: {
    show?: boolean;
    position?: "top" | "bottom" | "left" | "right";
  };
  tooltip?: {
    enabled?: boolean;
    formatter?: string;
  };
  colors?: string[];
  height?: number;
  width?: number;
}

export interface DrillDownLevel {
  level: number;
  name: string;
  field: string;
  parentValue?: string | number;
}

export interface DrillDownConfig {
  enabled: boolean;
  levels: DrillDownLevel[];
  currentLevel: number;
  breadcrumb: Array<{
    level: number;
    name: string;
    value: string | number;
  }>;
}

export interface ChartInteraction {
  type: "click" | "hover" | "select";
  data: ChartDataPoint;
  series: ChartSeries;
  chart: string;
}

export interface ChartLinkage {
  sourceChart: string;
  targetCharts: string[];
  linkField: string;
  filterType: "exact" | "range" | "contains";
}

export interface ChartFilter {
  field: string;
  operator: "eq" | "ne" | "gt" | "gte" | "lt" | "lte" | "in" | "contains";
  value: any;
  label?: string;
}

export interface ChartState {
  filters: ChartFilter[];
  drillDown: DrillDownConfig;
  selectedData?: ChartDataPoint[];
  isLoading: boolean;
  error?: string;
}

export interface InteractiveChart {
  id: string;
  title: string;
  type: "line" | "bar" | "pie" | "scatter" | "area" | "heatmap";
  config: ChartConfig;
  series: ChartSeries[];
  state: ChartState;
  linkages: ChartLinkage[];
  onInteraction?: (interaction: ChartInteraction) => void;
  onDrillDown?: (level: DrillDownLevel, value: any) => void;
  onFilter?: (filters: ChartFilter[]) => void;
}

export interface DashboardLayout {
  charts: InteractiveChart[];
  globalFilters: ChartFilter[];
  layout: {
    rows: number;
    cols: number;
    positions: Array<{
      chartId: string;
      x: number;
      y: number;
      w: number;
      h: number;
    }>;
  };
}

// 数据钻取相关类型
export interface DrillDownData {
  level: number;
  field: string;
  value: any;
  label: string;
  data: ChartDataPoint[];
  hasChildren: boolean;
}

export interface DrillDownPath {
  levels: Array<{
    field: string;
    value: any;
    label: string;
  }>;
}

// 图表联动相关类型
export interface LinkageEvent {
  sourceChartId: string;
  targetChartIds: string[];
  filters: ChartFilter[];
  timestamp: number;
}

export interface LinkageRule {
  id: string;
  name: string;
  sourceChart: string;
  targetCharts: string[];
  mapping: {
    sourceField: string;
    targetField: string;
  };
  condition?: {
    field: string;
    operator: string;
    value: any;
  };
  enabled: boolean;
}
