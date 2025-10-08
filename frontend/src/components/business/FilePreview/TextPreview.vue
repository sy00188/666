<template>
  <div class="text-preview">
    <!-- 加载状态 -->
    <div v-if="loading" class="text-preview__loading">
      <Loading type="ring" text="加载文件中..." />
    </div>

    <!-- 文本内容 -->
    <div v-show="!loading && !error" class="text-preview__container">
      <!-- 工具栏 -->
      <div v-if="showToolbar" class="text-preview__toolbar">
        <div class="toolbar-left">
          <span class="file-name">{{ filename }}</span>
          <el-tag v-if="language" size="small">{{ language }}</el-tag>
        </div>

        <div class="toolbar-right">
          <el-button-group>
            <el-button size="small" @click="decreaseFontSize">
              <el-icon><Minus /></el-icon>
            </el-button>
            <span class="font-size-display">{{ fontSize }}px</span>
            <el-button size="small" @click="increaseFontSize">
              <el-icon><Plus /></el-icon>
            </el-button>
          </el-button-group>

          <el-switch
            v-model="wordWrap"
            active-text="自动换行"
            inactive-text="不换行"
          />

          <el-button v-if="downloadable" @click="download">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>

      <!-- Monaco Editor（代码高亮） -->
      <div
        v-if="isCodeFile"
        ref="editorRef"
        class="text-preview__editor"
      />

      <!-- 纯文本显示 -->
      <pre v-else class="text-preview__content" :style="contentStyle">{{ content }}</pre>
    </div>

    <!-- 错误状态 -->
    <div v-if="error" class="text-preview__error">
      <Empty
        description="文件加载失败"
        icon="WarningFilled"
      >
        <el-button type="primary" @click="reload">重新加载</el-button>
      </Empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from 'vue';
import * as monaco from 'monaco-editor';
import { Plus, Minus, Download } from '@element-plus/icons-vue';
import { Loading, Empty } from '@/components/ui';
import { ElMessage } from 'element-plus';

/**
 * 文本/代码预览组件
 * 支持纯文本和代码高亮显示
 */

interface Props {
  /** 文件URL或文本内容 */
  source: string;
  /** 文件名（用于判断语言类型） */
  filename: string;
  /** 是否是代码文件 */
  isCode?: boolean;
  /** 指定的语言（如果不指定会自动检测） */
  language?: string;
  /** 是否显示工具栏 */
  showToolbar?: boolean;
  /** 是否允许下载 */
  downloadable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isCode: false,
  showToolbar: true,
  downloadable: true,
});

const emit = defineEmits<{
  loaded: [];
  error: [error: Error];
}>();

// 状态
const editorRef = ref<HTMLElement>();
const loading = ref(true);
const error = ref(false);
const content = ref('');
const fontSize = ref(14);
const wordWrap = ref(true);
let editorInstance: monaco.editor.IStandaloneCodeEditor | null = null;

// 是否是代码文件
const isCodeFile = computed(() => {
  if (props.isCode) return true;
  const ext = props.filename.split('.').pop()?.toLowerCase() || '';
  const codeExtensions = [
    'js', 'ts', 'jsx', 'tsx', 'vue', 'html', 'css', 'scss', 'less',
    'json', 'xml', 'yaml', 'yml', 'java', 'py', 'go', 'rs', 'c', 'cpp',
    'h', 'sql', 'sh', 'php', 'rb', 'swift', 'kt',
  ];
  return codeExtensions.includes(ext);
});

// 检测语言类型
const language = computed(() => {
  if (props.language) return props.language;
  
  const ext = props.filename.split('.').pop()?.toLowerCase() || '';
  const languageMap: Record<string, string> = {
    js: 'javascript',
    ts: 'typescript',
    jsx: 'javascript',
    tsx: 'typescript',
    vue: 'html',
    html: 'html',
    css: 'css',
    scss: 'scss',
    less: 'less',
    json: 'json',
    xml: 'xml',
    yaml: 'yaml',
    yml: 'yaml',
    md: 'markdown',
    java: 'java',
    py: 'python',
    go: 'go',
    rs: 'rust',
    c: 'c',
    cpp: 'cpp',
    h: 'c',
    sql: 'sql',
    sh: 'shell',
    php: 'php',
    rb: 'ruby',
    swift: 'swift',
    kt: 'kotlin',
  };
  
  return languageMap[ext] || 'plaintext';
});

// 内容样式
const contentStyle = computed(() => ({
  fontSize: `${fontSize.value}px`,
  whiteSpace: wordWrap.value ? 'pre-wrap' : 'pre',
}));

// 字体大小控制
const increaseFontSize = () => {
  if (fontSize.value < 24) {
    fontSize.value += 2;
    updateEditorFontSize();
  }
};

const decreaseFontSize = () => {
  if (fontSize.value > 10) {
    fontSize.value -= 2;
    updateEditorFontSize();
  }
};

const updateEditorFontSize = () => {
  if (editorInstance) {
    editorInstance.updateOptions({
      fontSize: fontSize.value,
    });
  }
};

// 下载
const download = () => {
  const blob = new Blob([content.value], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = props.filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  ElMessage.success('开始下载');
};

// 加载文件
const loadFile = async () => {
  loading.value = true;
  error.value = false;

  try {
    let text = '';
    
    // 如果source是URL，fetch获取内容
    if (props.source.startsWith('http') || props.source.startsWith('/')) {
      const response = await fetch(props.source);
      if (!response.ok) throw new Error('文件加载失败');
      text = await response.text();
    } else {
      // 否则认为source本身就是内容
      text = props.source;
    }

    content.value = text;

    // 如果是代码文件，初始化Monaco Editor
    if (isCodeFile.value) {
      await nextTick();
      initMonacoEditor();
    }

    loading.value = false;
    emit('loaded');
  } catch (err: any) {
    console.error('文件加载失败:', err);
    loading.value = false;
    error.value = true;
    emit('error', err);
    ElMessage.error('文件加载失败');
  }
};

// 初始化Monaco Editor
const initMonacoEditor = () => {
  if (!editorRef.value) return;

  // 销毁旧实例
  if (editorInstance) {
    editorInstance.dispose();
  }

  // 创建新实例
  editorInstance = monaco.editor.create(editorRef.value, {
    value: content.value,
    language: language.value,
    theme: 'vs',
    readOnly: true,
    fontSize: fontSize.value,
    wordWrap: wordWrap.value ? 'on' : 'off',
    minimap: { enabled: true },
    scrollBeyondLastLine: false,
    automaticLayout: true,
  });
};

// 重新加载
const reload = () => {
  fontSize.value = 14;
  wordWrap.value = true;
  loadFile();
};

// 监听换行设置
watch(wordWrap, (newVal) => {
  if (editorInstance) {
    editorInstance.updateOptions({
      wordWrap: newVal ? 'on' : 'off',
    });
  }
});

// 监听source变化
watch(() => props.source, () => {
  loadFile();
});

// 初始加载
onMounted(() => {
  loadFile();
});

// 清理
onUnmounted(() => {
  if (editorInstance) {
    editorInstance.dispose();
  }
});
</script>

<style lang="scss" scoped>
.text-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color);

  &__loading,
  &__error {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: var(--el-bg-color);
    border-bottom: 1px solid var(--el-border-color-light);
    flex-shrink: 0;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .file-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    .font-size-display {
      display: inline-block;
      min-width: 50px;
      text-align: center;
      font-size: 13px;
      color: var(--el-text-color-regular);
    }
  }

  &__editor {
    flex: 1;
    overflow: hidden;
  }

  &__content {
    flex: 1;
    overflow: auto;
    padding: 20px;
    margin: 0;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    line-height: 1.6;
    color: var(--el-text-color-primary);
    background: #fff;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .text-preview {
    &__toolbar {
      flex-direction: column;
      gap: 12px;

      .toolbar-left,
      .toolbar-right {
        width: 100%;
        justify-content: center;
      }
    }

    &__content {
      padding: 12px;
      font-size: 12px;
    }
  }
}
</style>

