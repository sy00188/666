import dayjs from "dayjs";

/**
 * 格式化日期时间
 * @param date 日期
 * @param format 格式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export const formatDateTime = (
  date: Date | string | number | null | undefined,
  format = "YYYY-MM-DD HH:mm:ss",
): string => {
  if (!date) return "";
  const dayjsDate = dayjs(date);
  if (!dayjsDate.isValid()) return "";
  return dayjsDate.format(format);
};

/**
 * 格式化日期
 * @param date 日期
 * @param format 格式，默认为 'YYYY-MM-DD'
 * @returns 格式化后的日期字符串
 */
export const formatDate = (
  date: Date | string | number | null | undefined,
  format = "YYYY-MM-DD",
): string => {
  if (!date) return "";
  const dayjsDate = dayjs(date);
  if (!dayjsDate.isValid()) return "";
  return dayjsDate.format(format);
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
): Date[] => {
  const startDate = dayjs(start);
  const endDate = dayjs(end);
  const dates: Date[] = [];

  let current = startDate;
  while (current.isBefore(endDate) || current.isSame(endDate)) {
    dates.push(current.toDate());
    current = current.add(1, "day");
  }

  return dates;
};

/**
 * 解析日期字符串
 * @param dateString 日期字符串
 * @returns 日期对象
 */
export const parseDate = (dateString: string): Date | null => {
  if (!dateString) return null;
  const parsed = dayjs(dateString);
  return parsed.isValid() ? parsed.toDate() : null;
};

/**
 * 验证日期是否有效
 * @param date 日期
 * @returns 是否有效
 */
export const isValidDate = (date: any): boolean => {
  return dayjs(date).isValid();
};

/**
 * 添加天数
 * @param date 日期
 * @param days 天数
 * @returns 新日期
 */
export const addDays = (date: Date | string, days: number): Date => {
  return dayjs(date).add(days, 'day').toDate();
};

/**
 * 减去天数
 * @param date 日期
 * @param days 天数
 * @returns 新日期
 */
export const subtractDays = (date: Date | string, days: number): Date => {
  return dayjs(date).subtract(days, 'day').toDate();
};

/**
 * 获取周范围
 * @param date 日期
 * @param startOfWeek 周开始日（0=周日，1=周一）
 * @returns 周范围
 */
export const getWeekRange = (date: Date | string, startOfWeek: number = 1): { start: Date; end: Date } => {
  const target = dayjs(date);
  const start = target.startOf('week').add(startOfWeek, 'day');
  const end = start.add(6, 'day');
  
  return {
    start: start.toDate(),
    end: end.toDate()
  };
};

/**
 * 获取月份范围
 * @param date 日期
 * @returns 月份范围
 */
export const getMonthRange = (date: Date | string): { start: Date; end: Date } => {
  const target = dayjs(date);
  
  return {
    start: target.startOf('month').toDate(),
    end: target.endOf('month').toDate()
  };
};

/**
 * 判断是否为同一天
 * @param date1 日期1
 * @param date2 日期2
 * @returns 是否为同一天
 */
export const isSameDay = (date1: Date | string, date2: Date | string): boolean => {
  if (!date1 || !date2) return false;
  return dayjs(date1).isSame(dayjs(date2), 'day');
};

/**
 * 计算日期差（天数）
 * @param date1 日期1
 * @param date2 日期2
 * @returns 天数差
 */
export const getDaysDiff = (date1: Date | string, date2: Date | string): number => {
  return dayjs(date1).diff(dayjs(date2), 'day');
};
