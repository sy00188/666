// 通用类型定义

// API响应基础接口
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data: T;
  code?: number;
  timestamp?: number;
}

// 分页查询参数
export interface PaginationParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
}

// 分页响应数据
export interface PaginatedResponse<T = unknown> {
  list: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}

// 选项接口
export interface Option<T = string | number> {
  label: string;
  value: T;
  disabled?: boolean;
  children?: Option<T>[];
}

// 树形节点接口
export interface TreeNode<T = unknown> {
  id: string | number;
  label: string;
  value?: T;
  children?: TreeNode<T>[];
  disabled?: boolean;
  checked?: boolean;
  indeterminate?: boolean;
  expanded?: boolean;
  level?: number;
  parent?: TreeNode<T>;
}

// 表格列配置
export interface TableColumn {
  prop: string;
  label: string;
  width?: string | number;
  minWidth?: string | number;
  fixed?: boolean | "left" | "right";
  sortable?: boolean | "custom";
  resizable?: boolean;
  showOverflowTooltip?: boolean;
  align?: "left" | "center" | "right";
  headerAlign?: "left" | "center" | "right";
  formatter?: (row: any, column: any, cellValue: any, index: number) => string;
  render?: (row: any, column: any, cellValue: any, index: number) => any;
}

// 表单验证规则
export interface FormRule {
  required?: boolean;
  message?: string;
  trigger?: "blur" | "change" | "submit";
  min?: number;
  max?: number;
  len?: number;
  pattern?: RegExp;
  validator?: (
    rule: any,
    value: any,
    callback: (error?: Error) => void,
  ) => void;
  type?:
    | "string"
    | "number"
    | "boolean"
    | "method"
    | "regexp"
    | "integer"
    | "float"
    | "array"
    | "object"
    | "enum"
    | "date"
    | "url"
    | "hex"
    | "email";
}

// 表单验证规则集合
export interface FormRules {
  [key: string]: FormRule | FormRule[];
}

// 文件上传接口
export interface UploadFile {
  id?: string;
  name: string;
  size: number;
  type: string;
  url?: string;
  status?: "ready" | "uploading" | "success" | "error";
  progress?: number;
  response?: any;
  error?: string;
  raw?: File;
}

// 菜单项接口
export interface MenuItem {
  id: string;
  name: string;
  path?: string;
  icon?: string;
  component?: string;
  redirect?: string;
  meta?: {
    title?: string;
    icon?: string;
    hidden?: boolean;
    keepAlive?: boolean;
    permission?: string[];
    roles?: string[];
    breadcrumb?: boolean;
    activeMenu?: string;
  };
  children?: MenuItem[];
}

// 面包屑项接口
export interface BreadcrumbItem {
  title: string;
  path?: string;
  disabled?: boolean;
}

// 统计卡片数据
export interface StatCard {
  title: string;
  value: string | number;
  icon?: string;
  color?: string;
  trend?: {
    value: number;
    type: "up" | "down";
    text?: string;
  };
  extra?: string;
}

// 图表数据点
export interface ChartDataPoint {
  name: string;
  value: number;
  [key: string]: any;
}

// 图表配置
export interface ChartConfig {
  title?: string;
  type: "line" | "bar" | "pie" | "area" | "scatter";
  data: ChartDataPoint[];
  xAxis?: string;
  yAxis?: string;
  colors?: string[];
  height?: number;
  width?: number;
}

// 操作按钮配置
export interface ActionButton {
  label: string;
  type?: "primary" | "success" | "warning" | "danger" | "info" | "text";
  icon?: string;
  disabled?: boolean;
  loading?: boolean;
  permission?: string;
  onClick: () => void;
}

// 批量操作配置
export interface BatchAction {
  label: string;
  value: string;
  type?: "primary" | "success" | "warning" | "danger" | "info";
  icon?: string;
  permission?: string;
  confirm?: {
    title: string;
    message: string;
    type?: "warning" | "info" | "success" | "error";
  };
}

// 搜索表单配置
export interface SearchFormItem {
  prop: string;
  label: string;
  type:
    | "input"
    | "select"
    | "date"
    | "daterange"
    | "datetime"
    | "datetimerange"
    | "cascader"
    | "checkbox"
    | "radio";
  placeholder?: string;
  options?: Option[];
  multiple?: boolean;
  clearable?: boolean;
  filterable?: boolean;
  remote?: boolean;
  remoteMethod?: (query: string) => void;
  format?: string;
  valueFormat?: string;
  props?: any;
}

// 导出配置
export interface ExportConfig {
  filename?: string;
  format: "excel" | "csv" | "pdf";
  columns?: string[];
  data?: any[];
  template?: string;
}

// 导入配置
export interface ImportConfig {
  accept: string;
  maxSize: number;
  template?: string;
  validateHeaders?: boolean;
  requiredColumns?: string[];
  columnMapping?: Record<string, string>;
}

// 系统配置项
export interface ConfigItem {
  key: string;
  value: any;
  type: "string" | "number" | "boolean" | "json" | "array";
  category: string;
  description?: string;
  required?: boolean;
  defaultValue?: any;
  options?: Option[];
  validation?: FormRule[];
}

// 日志级别
export enum LogLevel {
  DEBUG = "debug",
  INFO = "info",
  WARNING = "warning",
  ERROR = "error",
  CRITICAL = "critical",
}

// 操作类型
export enum OperationType {
  CREATE = "create",
  UPDATE = "update",
  DELETE = "delete",
  VIEW = "view",
  EXPORT = "export",
  IMPORT = "import",
  LOGIN = "login",
  LOGOUT = "logout",
}

// 状态枚举
export enum Status {
  ACTIVE = "active",
  INACTIVE = "inactive",
  PENDING = "pending",
  DISABLED = "disabled",
  DELETED = "deleted",
}

// 性别枚举
export enum Gender {
  MALE = "male",
  FEMALE = "female",
  OTHER = "other",
}

// 文件类型枚举
export enum FileType {
  IMAGE = "image",
  DOCUMENT = "document",
  VIDEO = "video",
  AUDIO = "audio",
  ARCHIVE = "archive",
  OTHER = "other",
}

// 通知类型
export enum NotificationType {
  INFO = "info",
  SUCCESS = "success",
  WARNING = "warning",
  ERROR = "error",
}

// 主题类型
export enum ThemeType {
  LIGHT = "light",
  DARK = "dark",
  AUTO = "auto",
}

// 语言类型
export enum LanguageType {
  ZH_CN = "zh-CN",
  EN_US = "en-US",
}

// 时间格式
export const TIME_FORMATS = {
  DATE: "YYYY-MM-DD",
  DATETIME: "YYYY-MM-DD HH:mm:ss",
  TIME: "HH:mm:ss",
  MONTH: "YYYY-MM",
  YEAR: "YYYY",
} as const;

// 文件大小单位
export const FILE_SIZE_UNITS = ["B", "KB", "MB", "GB", "TB"] as const;

// 默认分页配置
export const DEFAULT_PAGINATION = {
  page: 1,
  size: 20,
  sizes: [10, 20, 50, 100],
  layout: "total, sizes, prev, pager, next, jumper",
} as const;
