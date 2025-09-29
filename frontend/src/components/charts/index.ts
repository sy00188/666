// 高级图表组件库统一导出
export { default as HeatmapChart } from "./HeatmapChart.vue";
export { default as RadarChart } from "./RadarChart.vue";
export { default as GaugeChart } from "./GaugeChart.vue";
export { default as SankeyChart } from "./SankeyChart.vue";
export { default as TreemapChart } from "./TreemapChart.vue";

// 图表组件类型定义
export interface ChartComponentProps {
  height?: string;
  title?: string;
  loading?: boolean;
}

export interface ChartEmits {
  (e: "refresh"): void;
  (e: "chartClick", params: any): void;
}

// 热力图相关类型
export interface HeatmapData {
  name: string;
  value: [number, number, number];
}

export interface CalendarHeatmapData {
  date: string;
  value: number;
}

// 雷达图相关类型
export interface RadarIndicator {
  name: string;
  max: number;
  min?: number;
}

export interface RadarSeriesData {
  name: string;
  value: number[];
  areaStyle?: {
    opacity?: number;
  };
}

// 仪表盘相关类型
export interface GaugeData {
  name: string;
  value: number;
  unit?: string;
}

// 桑基图相关类型
export interface SankeyNode {
  name: string;
  value?: number;
  depth?: number;
  itemStyle?: {
    color?: string;
  };
}

export interface SankeyLink {
  source: string;
  target: string;
  value: number;
  lineStyle?: {
    color?: string;
    opacity?: number;
  };
}

export interface SankeyData {
  nodes: SankeyNode[];
  links: SankeyLink[];
}

// 树状图相关类型
export interface TreemapNode {
  name: string;
  value?: number;
  children?: TreemapNode[];
  itemStyle?: {
    color?: string;
    borderColor?: string;
    borderWidth?: number;
  };
  label?: {
    show?: boolean;
    position?: string;
    fontSize?: number;
    color?: string;
  };
}
