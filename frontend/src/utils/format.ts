/**
 * 格式化工具函数
 */

/**
 * 格式化日期时间
 * @param dateTime 日期时间字符串或Date对象
 * @param format 格式化模式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期时间字符串
 */
export function formatDateTime(
  dateTime?: string | Date | null,
  format: string = "YYYY-MM-DD HH:mm:ss",
): string {
  if (!dateTime) {
    return "-";
  }

  const date = typeof dateTime === "string" ? new Date(dateTime) : dateTime;

  if (isNaN(date.getTime())) {
    return "-";
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");

  switch (format) {
    case "YYYY-MM-DD":
      return `${year}-${month}-${day}`;
    case "HH:mm:ss":
      return `${hours}:${minutes}:${seconds}`;
    case "MM-DD HH:mm":
      return `${month}-${day} ${hours}:${minutes}`;
    case "YYYY-MM-DD HH:mm":
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    default:
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }
}

/**
 * 格式化文件大小
 * @param size 文件大小（字节）
 * @returns 格式化后的文件大小字符串
 */
export function formatFileSize(size?: number | null): string {
  if (!size || size === 0) {
    return "0 B";
  }

  if (size < 0) {
    return "Unknown";
  }

  const units = ["B", "KB", "MB", "GB", "TB"];
  let unitIndex = 0;
  let fileSize = size;

  while (fileSize >= 1024 && unitIndex < units.length - 1) {
    fileSize /= 1024;
    unitIndex++;
  }

  return `${fileSize.toFixed(2)} ${units[unitIndex]}`;
}

/**
 * 格式化相对时间
 * @param dateTime 日期时间字符串或Date对象
 * @returns 相对时间字符串（如：刚刚、5分钟前、2小时前等）
 */
export function formatRelativeTime(dateTime?: string | Date | null): string {
  if (!dateTime) {
    return "-";
  }

  const date = typeof dateTime === "string" ? new Date(dateTime) : dateTime;

  if (isNaN(date.getTime())) {
    return "-";
  }

  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  const months = Math.floor(days / 30);
  const years = Math.floor(months / 12);

  if (seconds < 60) {
    return "刚刚";
  } else if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else if (days < 30) {
    return `${days}天前`;
  } else if (months < 12) {
    return `${months}个月前`;
  } else {
    return `${years}年前`;
  }
}

/**
 * 格式化数字
 * @param num 数字
 * @param precision 小数位数，默认为2
 * @returns 格式化后的数字字符串
 */
export function formatNumber(
  num?: number | null,
  precision: number = 2,
): string {
  if (num === null || num === undefined) {
    return "0";
  }

  if (num >= 10000) {
    return `${(num / 10000).toFixed(precision)}万`;
  } else if (num >= 1000) {
    return `${(num / 1000).toFixed(precision)}k`;
  } else {
    return num.toString();
  }
}

/**
 * 格式化百分比
 * @param value 数值
 * @param total 总数
 * @param precision 小数位数，默认为1
 * @returns 格式化后的百分比字符串
 */
export function formatPercentage(
  value?: number | null,
  total?: number | null,
  precision: number = 1,
): string {
  if (!value || !total || total === 0) {
    return "0%";
  }

  const percentage = (value / total) * 100;
  return `${percentage.toFixed(precision)}%`;
}

/**
 * 格式化时长
 * @param duration 时长（秒）
 * @returns 格式化后的时长字符串（如：01:23:45）
 */
export function formatDuration(duration?: number | null): string {
  if (!duration || duration === 0) {
    return "00:00:00";
  }

  const hours = Math.floor(duration / 3600);
  const minutes = Math.floor((duration % 3600) / 60);
  const seconds = Math.floor(duration % 60);

  return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

/**
 * 格式化货币
 * @param amount 金额
 * @param currency 货币符号，默认为'¥'
 * @param precision 小数位数，默认为2
 * @returns 格式化后的货币字符串
 */
export function formatCurrency(
  amount?: number | null,
  currency: string = "¥",
  precision: number = 2,
): string {
  if (amount === null || amount === undefined) {
    return `${currency}0.00`;
  }

  return `${currency}${amount.toFixed(precision)}`;
}
