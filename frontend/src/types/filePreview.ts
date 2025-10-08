// 文件预览相关类型定义

/**
 * 文件类型枚举
 */
export enum FileType {
    /** PDF文档 */
    PDF = 'pdf',
    /** Word文档 */
    WORD = 'word',
    /** Excel表格 */
    EXCEL = 'excel',
    /** PowerPoint演示文稿 */
    PPT = 'ppt',
    /** 图片 */
    IMAGE = 'image',
    /** 文本 */
    TEXT = 'text',
    /** 代码 */
    CODE = 'code',
    /** 视频 */
    VIDEO = 'video',
    /** 音频 */
    AUDIO = 'audio',
    /** 压缩包 */
    ARCHIVE = 'archive',
    /** 其他 */
    OTHER = 'other',
}

/**
 * 预览模式
 */
export enum PreviewMode {
    /** 对话框模式 */
    DIALOG = 'dialog',
    /** 全屏模式 */
    FULLSCREEN = 'fullscreen',
    /** 内联模式 */
    INLINE = 'inline',
    /** 新窗口模式 */
    WINDOW = 'window',
}

/**
 * 文件信息接口
 */
export interface FileInfo {
    /** 文件ID */
    id: number | string;
    /** 文件名 */
    name: string;
    /** 文件大小（字节） */
    size: number;
    /** 文件类型 */
    type: FileType;
    /** MIME类型 */
    mimeType: string;
    /** 文件扩展名 */
    extension: string;
    /** 文件URL */
    url: string;
    /** 缩略图URL */
    thumbnailUrl?: string;
    /** 文件路径 */
    path?: string;
    /** 上传时间 */
    uploadedAt?: string;
    /** 上传者 */
    uploader?: string;
    /** 文件描述 */
    description?: string;
    /** 下载次数 */
    downloadCount?: number;
    /** 预览次数 */
    viewCount?: number;
    /** 是否加密 */
    encrypted?: boolean;
    /** 是否有水印 */
    watermark?: boolean;
    /** 额外元数据 */
    metadata?: Record<string, any>;
}

/**
 * 预览配置
 */
export interface PreviewConfig {
    /** 预览模式 */
    mode?: PreviewMode;
    /** 是否显示工具栏 */
    toolbar?: boolean;
    /** 是否允许下载 */
    downloadable?: boolean;
    /** 是否允许打印 */
    printable?: boolean;
    /** 是否显示文件信息 */
    showInfo?: boolean;
    /** 初始缩放比例 */
    initialScale?: number;
    /** 最小缩放比例 */
    minScale?: number;
    /** 最大缩放比例 */
    maxScale?: number;
    /** 缩放步长 */
    scaleStep?: number;
    /** 是否启用水印 */
    watermark?: boolean | WatermarkConfig;
    /** 主题 */
    theme?: 'light' | 'dark' | 'auto';
    /** 语言 */
    locale?: string;
    /** 自定义样式类名 */
    customClass?: string;
    /** 加载失败时的回退内容 */
    fallback?: string | (() => void);
}

/**
 * 水印配置
 */
export interface WatermarkConfig {
    /** 水印文本 */
    text: string;
    /** 字体大小 */
    fontSize?: number;
    /** 字体颜色 */
    color?: string;
    /** 透明度 */
    opacity?: number;
    /** 旋转角度 */
    rotate?: number;
    /** 水平间距 */
    gapX?: number;
    /** 垂直间距 */
    gapY?: number;
}

/**
 * PDF预览配置
 */
export interface PdfPreviewConfig extends PreviewConfig {
    /** 初始页码 */
    page?: number;
    /** 是否显示页码导航 */
    showPageNav?: boolean;
    /** 是否允许文本选择 */
    textLayerMode?: 0 | 1 | 2; // 0: 禁用, 1: 启用, 2: 增强
    /** 旋转角度（0, 90, 180, 270） */
    rotation?: number;
}

/**
 * 图片预览配置
 */
export interface ImagePreviewConfig extends PreviewConfig {
    /** 是否显示缩略图列表 */
    showThumbnails?: boolean;
    /** 是否循环浏览 */
    loop?: boolean;
    /** 是否显示图片索引 */
    showIndex?: boolean;
    /** 切换动画 */
    transition?: string;
    /** 是否允许旋转 */
    rotatable?: boolean;
    /** 是否允许翻转 */
    flippable?: boolean;
    /** 背景颜色 */
    backgroundColor?: string;
}

/**
 * 代码预览配置
 */
export interface CodePreviewConfig extends PreviewConfig {
    /** 编程语言 */
    language?: string;
    /** 主题 */
    theme?: string;
    /** 是否显示行号 */
    lineNumbers?: boolean;
    /** 是否自动换行 */
    wordWrap?: boolean;
    /** 是否只读 */
    readOnly?: boolean;
    /** 字体大小 */
    fontSize?: number;
    /** Tab大小 */
    tabSize?: number;
}

/**
 * 文本预览配置
 */
export interface TextPreviewConfig extends PreviewConfig {
    /** 编码格式 */
    encoding?: string;
    /** 是否自动换行 */
    wordWrap?: boolean;
    /** 字体大小 */
    fontSize?: number;
    /** 行高 */
    lineHeight?: number;
}

/**
 * 预览状态
 */
export enum PreviewStatus {
    /** 空闲 */
    IDLE = 'idle',
    /** 加载中 */
    LOADING = 'loading',
    /** 成功 */
    SUCCESS = 'success',
    /** 失败 */
    ERROR = 'error',
}

/**
 * 预览响应
 */
export interface PreviewResponse {
    /** 状态 */
    status: PreviewStatus;
    /** 文件信息 */
    fileInfo?: FileInfo;
    /** 预览数据（可能是URL或base64） */
    data?: string | ArrayBuffer;
    /** 错误信息 */
    error?: string;
    /** 是否支持预览 */
    supported: boolean;
    /** 建议的预览方式 */
    suggestedAction?: 'preview' | 'download' | 'external';
}

/**
 * 文件扩展名与类型映射
 */
export const FILE_TYPE_MAP: Record<string, FileType> = {
    // PDF
    pdf: FileType.PDF,

    // Word
    doc: FileType.WORD,
    docx: FileType.WORD,

    // Excel
    xls: FileType.EXCEL,
    xlsx: FileType.EXCEL,
    csv: FileType.EXCEL,

    // PowerPoint
    ppt: FileType.PPT,
    pptx: FileType.PPT,

    // 图片
    jpg: FileType.IMAGE,
    jpeg: FileType.IMAGE,
    png: FileType.IMAGE,
    gif: FileType.IMAGE,
    bmp: FileType.IMAGE,
    webp: FileType.IMAGE,
    svg: FileType.IMAGE,
    ico: FileType.IMAGE,

    // 文本
    txt: FileType.TEXT,
    md: FileType.TEXT,

    // 代码
    js: FileType.CODE,
    ts: FileType.CODE,
    jsx: FileType.CODE,
    tsx: FileType.CODE,
    vue: FileType.CODE,
    html: FileType.CODE,
    css: FileType.CODE,
    scss: FileType.CODE,
    less: FileType.CODE,
    json: FileType.CODE,
    xml: FileType.CODE,
    yaml: FileType.CODE,
    yml: FileType.CODE,
    java: FileType.CODE,
    py: FileType.CODE,
    go: FileType.CODE,
    rs: FileType.CODE,
    c: FileType.CODE,
    cpp: FileType.CODE,
    h: FileType.CODE,
    sql: FileType.CODE,
    sh: FileType.CODE,

    // 视频
    mp4: FileType.VIDEO,
    avi: FileType.VIDEO,
    mov: FileType.VIDEO,
    wmv: FileType.VIDEO,
    flv: FileType.VIDEO,
    mkv: FileType.VIDEO,

    // 音频
    mp3: FileType.AUDIO,
    wav: FileType.AUDIO,
    ogg: FileType.AUDIO,
    flac: FileType.AUDIO,
    aac: FileType.AUDIO,

    // 压缩包
    zip: FileType.ARCHIVE,
    rar: FileType.ARCHIVE,
    '7z': FileType.ARCHIVE,
    tar: FileType.ARCHIVE,
    gz: FileType.ARCHIVE,
};

/**
 * 获取文件类型
 */
export function getFileType(filename: string): FileType {
    const ext = filename.split('.').pop()?.toLowerCase() || '';
    return FILE_TYPE_MAP[ext] || FileType.OTHER;
}

/**
 * 判断文件是否支持预览
 */
export function isPreviewSupported(fileType: FileType): boolean {
    return [
        FileType.PDF,
        FileType.WORD,
        FileType.EXCEL,
        FileType.IMAGE,
        FileType.TEXT,
        FileType.CODE,
    ].includes(fileType);
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';

    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * 预览工具栏操作
 */
export interface PreviewToolbarAction {
    /** 操作名称 */
    name: string;
    /** 操作图标 */
    icon: string;
    /** 操作标签 */
    label?: string;
    /** 是否禁用 */
    disabled?: boolean;
    /** 点击处理函数 */
    handler: () => void;
}

