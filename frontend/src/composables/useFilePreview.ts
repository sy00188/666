// 文件预览Composable

import { ref, computed } from 'vue';
import type {
  FileInfo,
  FileType,
  PreviewConfig,
  PreviewStatus,
  PreviewResponse,
} from '@/types/filePreview';
import { getFileType, isPreviewSupported, formatFileSize } from '@/types/filePreview';
import { ElMessage } from 'element-plus';

/**
 * 文件预览Composable
 * 提供文件预览的通用逻辑和状态管理
 */
export function useFilePreview() {
  // 当前预览的文件
  const currentFile = ref<FileInfo | null>(null);
  
  // 预览状态
  const previewStatus = ref<PreviewStatus>('idle');
  
  // 是否显示预览对话框
  const showPreview = ref(false);
  
  // 预览配置
  const previewConfig = ref<PreviewConfig>({
    mode: 'dialog',
    toolbar: true,
    downloadable: true,
    printable: true,
    showInfo: true,
  });

  // 当前文件类型
  const currentFileType = computed<FileType | null>(() => {
    if (!currentFile.value) return null;
    return currentFile.value.type || getFileType(currentFile.value.name);
  });

  // 是否支持预览
  const canPreview = computed(() => {
    if (!currentFileType.value) return false;
    return isPreviewSupported(currentFileType.value);
  });

  // 格式化的文件大小
  const formattedFileSize = computed(() => {
    if (!currentFile.value) return '';
    return formatFileSize(currentFile.value.size);
  });

  /**
   * 打开文件预览
   */
  const openPreview = (fileInfo: FileInfo, config?: Partial<PreviewConfig>) => {
    currentFile.value = fileInfo;
    
    if (config) {
      previewConfig.value = { ...previewConfig.value, ...config };
    }

    // 检查是否支持预览
    const fileType = fileInfo.type || getFileType(fileInfo.name);
    if (!isPreviewSupported(fileType)) {
      ElMessage.warning('该文件类型暂不支持在线预览，请下载后查看');
      downloadFile(fileInfo);
      return;
    }

    showPreview.value = true;
    previewStatus.value = 'loading';
  };

  /**
   * 关闭预览
   */
  const closePreview = () => {
    showPreview.value = false;
    previewStatus.value = 'idle';
    
    // 延迟清空文件信息，确保关闭动画完成
    setTimeout(() => {
      currentFile.value = null;
    }, 300);
  };

  /**
   * 预览加载完成
   */
  const onPreviewLoaded = () => {
    previewStatus.value = 'success';
  };

  /**
   * 预览加载失败
   */
  const onPreviewError = (error?: string) => {
    previewStatus.value = 'error';
    ElMessage.error(error || '文件预览失败');
  };

  /**
   * 下载文件
   */
  const downloadFile = (fileInfo: FileInfo) => {
    if (!fileInfo.url) {
      ElMessage.error('文件地址不存在');
      return;
    }

    try {
      const link = document.createElement('a');
      link.href = fileInfo.url;
      link.download = fileInfo.name;
      link.target = '_blank';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      ElMessage.success('开始下载文件');
    } catch (error) {
      console.error('文件下载失败:', error);
      ElMessage.error('文件下载失败');
    }
  };

  /**
   * 打印文件
   */
  const printFile = () => {
    if (!currentFile.value) return;
    
    try {
      window.print();
    } catch (error) {
      console.error('打印失败:', error);
      ElMessage.error('打印失败');
    }
  };

  /**
   * 全屏切换
   */
  const toggleFullscreen = () => {
    const elem = document.documentElement;
    
    if (!document.fullscreenElement) {
      elem.requestFullscreen().catch((err) => {
        console.error('进入全屏失败:', err);
        ElMessage.error('进入全屏失败');
      });
    } else {
      document.exitFullscreen().catch((err) => {
        console.error('退出全屏失败:', err);
        ElMessage.error('退出全屏失败');
      });
    }
  };

  /**
   * 获取文件图标
   */
  const getFileIcon = (fileType: FileType): string => {
    const iconMap: Record<FileType, string> = {
      pdf: 'DocumentCopy',
      word: 'Document',
      excel: 'Grid',
      ppt: 'Present',
      image: 'Picture',
      text: 'Document',
      code: 'Document',
      video: 'VideoCamera',
      audio: 'Headset',
      archive: 'FolderOpened',
      other: 'Document',
    };
    
    return iconMap[fileType] || 'Document';
  };

  /**
   * 获取文件颜色
   */
  const getFileColor = (fileType: FileType): string => {
    const colorMap: Record<FileType, string> = {
      pdf: '#FF0000',
      word: '#2B579A',
      excel: '#217346',
      ppt: '#D24726',
      image: '#67C23A',
      text: '#909399',
      code: '#E6A23C',
      video: '#F56C6C',
      audio: '#409EFF',
      archive: '#909399',
      other: '#909399',
    };
    
    return colorMap[fileType] || '#909399';
  };

  return {
    // 状态
    currentFile,
    previewStatus,
    showPreview,
    previewConfig,
    currentFileType,
    canPreview,
    formattedFileSize,

    // 方法
    openPreview,
    closePreview,
    onPreviewLoaded,
    onPreviewError,
    downloadFile,
    printFile,
    toggleFullscreen,
    getFileIcon,
    getFileColor,
  };
}

/**
 * 获取预览URL
 * 根据文件信息生成预览URL
 */
export function getPreviewUrl(fileInfo: FileInfo): string {
  // 如果已有URL直接返回
  if (fileInfo.url) {
    return fileInfo.url;
  }

  // 根据文件路径生成URL
  if (fileInfo.path) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
    return `${baseURL}/files/preview?path=${encodeURIComponent(fileInfo.path)}`;
  }

  return '';
}

/**
 * 检测文件MIME类型
 */
export function detectMimeType(filename: string): string {
  const ext = filename.split('.').pop()?.toLowerCase() || '';
  
  const mimeMap: Record<string, string> = {
    // PDF
    pdf: 'application/pdf',
    
    // Office
    doc: 'application/msword',
    docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    xls: 'application/vnd.ms-excel',
    xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    ppt: 'application/vnd.ms-powerpoint',
    pptx: 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    
    // 图片
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    png: 'image/png',
    gif: 'image/gif',
    bmp: 'image/bmp',
    webp: 'image/webp',
    svg: 'image/svg+xml',
    
    // 文本
    txt: 'text/plain',
    md: 'text/markdown',
    html: 'text/html',
    css: 'text/css',
    js: 'text/javascript',
    json: 'application/json',
    xml: 'application/xml',
    
    // 视频
    mp4: 'video/mp4',
    avi: 'video/x-msvideo',
    mov: 'video/quicktime',
    
    // 音频
    mp3: 'audio/mpeg',
    wav: 'audio/wav',
    ogg: 'audio/ogg',
    
    // 压缩包
    zip: 'application/zip',
    rar: 'application/x-rar-compressed',
    '7z': 'application/x-7z-compressed',
  };
  
  return mimeMap[ext] || 'application/octet-stream';
}

