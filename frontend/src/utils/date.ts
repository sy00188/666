import dayjs from "dayjs";

/**
 * 格式化日期时间
 * @param date 日期
 * @param format 格式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export const formatDateTime = (
  date: Date | string | number,
  format = "YYYY-MM-DD HH:mm:ss",
): string => {
  if (!date) return "";
  return dayjs(date).format(format);
};

/**
 * 格式化日期
 * @param date 日期
 * @param format 格式，默认为 'YYYY-MM-DD'
 * @returns 格式化后的日期字符串
 */
export const formatDate = (
  date: Date | string | number,
  format = "YYYY-MM-DD",
): string => {
  if (!date) return "";
  return dayjs(date).format(format);
};

/**
 * 格式化时间
 * @param date 日期
 * @param format 格式，默认为 'HH:mm:ss'
 * @returns 格式化后的时间字符串
 */
export const formatTime = (
  date: Date | string | number,
  format = "HH:mm:ss",
): string => {
  if (!date) return "";
  return dayjs(date).format(format);
};

/**
 * 获取相对时间
 * @param date 日期
 * @returns 相对时间字符串
 */
export const getRelativeTime = (date: Date | string | number): string => {
  if (!date) return "";
  const now = dayjs();
  const target = dayjs(date);
  const diff = now.diff(target, "second");

  if (diff < 60) {
    return "刚刚";
  } else if (diff < 3600) {
    return `${Math.floor(diff / 60)}分钟前`;
  } else if (diff < 86400) {
    return `${Math.floor(diff / 3600)}小时前`;
  } else if (diff < 2592000) {
    return `${Math.floor(diff / 86400)}天前`;
  } else {
    return formatDate(date);
  }
};

/**
 * 判断是否为今天
 * @param date 日期
 * @returns 是否为今天
 */
export const isToday = (date: Date | string | number): boolean => {
  if (!date) return false;
  return dayjs(date).isSame(dayjs(), "day");
};

/**
 * 判断是否为本周
 * @param date 日期
 * @returns 是否为本周
 */
export const isThisWeek = (date: Date | string | number): boolean => {
  if (!date) return false;
  return dayjs(date).isSame(dayjs(), "week");
};

/**
 * 判断是否为本月
 * @param date 日期
 * @returns 是否为本月
 */
export const isThisMonth = (date: Date | string | number): boolean => {
  if (!date) return false;
  return dayjs(date).isSame(dayjs(), "month");
};

/**
 * 获取日期范围
 * @param start 开始日期
 * @param end 结束日期
 * @returns 日期范围数组
 */
export const getDateRange = (
  start: Date | string,
  end: Date | string,
): string[] => {
  const startDate = dayjs(start);
  const endDate = dayjs(end);
  const dates: string[] = [];

  let current = startDate;
  while (current.isBefore(endDate) || current.isSame(endDate)) {
    dates.push(current.format("YYYY-MM-DD"));
    current = current.add(1, "day");
  }

  return dates;
};
