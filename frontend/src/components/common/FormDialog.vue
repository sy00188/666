<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :fullscreen="fullscreen"
    :modal="modal"
    :modal-class="modalClass"
    :append-to-body="appendToBody"
    :lock-scroll="lockScroll"
    :custom-class="customClass"
    :open-delay="openDelay"
    :close-delay="closeDelay"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="showClose"
    :before-close="handleBeforeClose"
    :center="center"
    :align-center="alignCenter"
    :destroy-on-close="destroyOnClose"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 自定义头部 -->
    <template v-if="$slots.header" #header="{ close, titleId, titleClass }">
      <slot
        name="header"
        :close="close"
        :title-id="titleId"
        :title-class="titleClass"
      />
    </template>

    <!-- 表单内容 -->
    <div class="form-dialog-content">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-width="labelWidth"
        :label-position="labelPosition"
        :inline="inline"
        :size="size"
        :disabled="disabled"
        :validate-on-rule-change="validateOnRuleChange"
        :hide-required-asterisk="hideRequiredAsterisk"
        :show-message="showMessage"
        :inline-message="inlineMessage"
        :status-icon="statusIcon"
        :scroll-to-error="scrollToError"
        @validate="handleValidate"
      >
        <!-- 动态表单项 -->
        <template v-for="field in fields" :key="field.prop">
          <!-- 分组标题 -->
          <div v-if="field.type === 'group'" class="form-group-title">
            <el-divider content-position="left">
              {{ field.label }}
            </el-divider>
          </div>

          <!-- 普通表单项 -->
          <el-form-item
            v-else
            :prop="field.prop"
            :label="field.label"
            :label-width="field.labelWidth"
            :required="field.required"
            :rules="field.rules"
            :error="field.error"
            :show-message="field.showMessage !== false"
            :inline-message="field.inlineMessage"
            :size="field.size || size"
            :class="field.class"
          >
            <!-- 自定义表单项内容 -->
            <slot
              v-if="field.slot"
              :name="field.prop"
              :field="field"
              :value="formData[field.prop]"
              :form-data="formData"
            />

            <!-- 输入框 -->
            <el-input
              v-else-if="field.type === 'input' || !field.type"
              v-model="formData[field.prop]"
              :type="field.inputType || 'text'"
              :placeholder="field.placeholder"
              :clearable="field.clearable !== false"
              :show-password="field.showPassword"
              :show-word-limit="field.showWordLimit"
              :maxlength="field.maxlength"
              :minlength="field.minlength"
              :prefix-icon="field.prefixIcon"
              :suffix-icon="field.suffixIcon"
              :readonly="field.readonly"
              :disabled="field.disabled"
              :size="field.size || size"
              :resize="field.resize"
              :autosize="field.autosize"
              :autocomplete="field.autocomplete"
              :name="field.name"
              :max="field.max"
              :min="field.min"
              :step="field.step"
              :controls="field.controls"
              :controls-position="field.controlsPosition"
              :precision="field.precision"
              @blur="handleFieldBlur(field, $event)"
              @focus="handleFieldFocus(field, $event)"
              @change="handleFieldChange(field, $event)"
              @input="handleFieldInput(field, $event)"
              @clear="handleFieldClear(field)"
            >
              <!-- 前置内容 -->
              <template v-if="field.prepend" #prepend>
                {{ field.prepend }}
              </template>
              <!-- 后置内容 -->
              <template v-if="field.append" #append>
                {{ field.append }}
              </template>
            </el-input>

            <!-- 文本域 -->
            <el-input
              v-else-if="field.type === 'textarea'"
              v-model="formData[field.prop]"
              type="textarea"
              :placeholder="field.placeholder"
              :rows="field.rows || 3"
              :autosize="field.autosize"
              :maxlength="field.maxlength"
              :minlength="field.minlength"
              :show-word-limit="field.showWordLimit"
              :readonly="field.readonly"
              :disabled="field.disabled"
              :resize="field.resize"
              @blur="handleFieldBlur(field, $event)"
              @focus="handleFieldFocus(field, $event)"
              @change="handleFieldChange(field, $event)"
              @input="handleFieldInput(field, $event)"
            />

            <!-- 数字输入框 -->
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="formData[field.prop]"
              :min="field.min"
              :max="field.max"
              :step="field.step || 1"
              :precision="field.precision"
              :size="field.size || size"
              :disabled="field.disabled"
              :controls="field.controls !== false"
              :controls-position="field.controlsPosition"
              :placeholder="field.placeholder"
              @change="handleFieldChange(field, $event)"
              @blur="handleFieldBlur(field, $event)"
              @focus="handleFieldFocus(field, $event)"
            />

            <!-- 选择器 -->
            <el-select
              v-else-if="field.type === 'select'"
              v-model="formData[field.prop]"
              :placeholder="field.placeholder"
              :multiple="field.multiple"
              :disabled="field.disabled"
              :clearable="field.clearable !== false"
              :collapse-tags="field.collapseTags"
              :collapse-tags-tooltip="field.collapseTagsTooltip"
              :multiple-limit="field.multipleLimit"
              :size="field.size || size"
              :filterable="field.filterable"
              :allow-create="field.allowCreate"
              :filter-method="field.filterMethod"
              :remote="field.remote"
              :remote-method="field.remoteMethod"
              :loading="field.loading"
              :loading-text="field.loadingText"
              :no-match-text="field.noMatchText"
              :no-data-text="field.noDataText"
              :popper-class="field.popperClass"
              :reserve-keyword="field.reserveKeyword"
              :default-first-option="field.defaultFirstOption"
              :teleported="field.teleported !== false"
              :automatic-dropdown="field.automaticDropdown"
              @change="handleFieldChange(field, $event)"
              @visible-change="handleSelectVisibleChange(field, $event)"
              @remove-tag="handleSelectRemoveTag(field, $event)"
              @clear="handleFieldClear(field)"
              @blur="handleFieldBlur(field, $event)"
              @focus="handleFieldFocus(field, $event)"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
                :disabled="option.disabled"
              />
            </el-select>

            <!-- 级联选择器 -->
            <el-cascader
              v-else-if="field.type === 'cascader'"
              v-model="formData[field.prop]"
              :options="field.options"
              :placeholder="field.placeholder"
              :size="field.size || size"
              :disabled="field.disabled"
              :clearable="field.clearable !== false"
              :show-all-levels="field.showAllLevels !== false"
              :collapse-tags="field.collapseTags"
              :collapse-tags-tooltip="field.collapseTagsTooltip"
              :separator="field.separator || ' / '"
              :filterable="field.filterable"
              :debounce="field.debounce"
              :before-filter="field.beforeFilter"
              :popper-class="field.popperClass"
              :teleported="field.teleported !== false"
              @change="handleFieldChange(field, $event)"
              @expand-change="handleCascaderExpandChange(field, $event)"
              @blur="handleFieldBlur(field, $event)"
              @focus="handleFieldFocus(field, $event)"
              @visible-change="handleCascaderVisibleChange(field, $event)"
            />

            <!-- 日期选择器 -->
            <el-date-picker
              v-else-if="field.type === 'date'"
              v-model="formData[field.prop]"
              :type="field.dateType || 'date'"
              :placeholder="field.placeholder"
              :start-placeholder="field.startPlaceholder"
              :end-placeholder="field.endPlaceholder"
              :format="field.format"
              :value-format="field.valueFormat"
              :size="field.size || size"
              :disabled="field.disabled"
              :clearable="field.clearable !== false"
              :readonly="field.readonly"
              :editable="field.editable !== false"
              :disabled-date="field.disabledDate"
              :shortcuts="field.shortcuts"
              :cell-class-name="field.cellClassName"
              :range-separator="field.rangeSeparator"
              :default-value="field.defaultValue"
              :default-time="field.defaultTime"
              :popper-class="field.popperClass"
              :teleported="field.teleported !== false"
              @change="handleFieldChange(field, $event)"
              @blur="handleFieldBlur(field, $event)"
              @focus="handleFieldFocus(field, $event)"
              @calendar-change="handleDateCalendarChange(field, $event)"
              @panel-change="handleDatePanelChange(field, $event)"
              @visible-change="handleDateVisibleChange(field, $event)"
            />

            <!-- 时间选择器 -->
            <el-time-picker
              v-else-if="field.type === 'time'"
              v-model="formData[field.prop]"
              :placeholder="field.placeholder"
              :start-placeholder="field.startPlaceholder"
              :end-placeholder="field.endPlaceholder"
              :is-range="field.isRange"
              :arrow-control="field.arrowControl"
              :format="field.format"
              :value-format="field.valueFormat"
              :size="field.size || size"
              :disabled="field.disabled"
              :clearable="field.clearable !== false"
              :readonly="field.readonly"
              :editable="field.editable !== false"
              :disabled-hours="field.disabledHours"
              :disabled-minutes="field.disabledMinutes"
              :disabled-seconds="field.disabledSeconds"
              :range-separator="field.rangeSeparator"
              :popper-class="field.popperClass"
              :teleported="field.teleported !== false"
              @change="handleFieldChange(field, $event)"
              @blur="handleFieldBlur(field, $event)"
              @focus="handleFieldFocus(field, $event)"
              @visible-change="handleTimeVisibleChange(field, $event)"
            />

            <!-- 开关 -->
            <el-switch
              v-else-if="field.type === 'switch'"
              v-model="formData[field.prop]"
              :size="field.size || size"
              :disabled="field.disabled"
              :loading="field.loading"
              :width="field.width"
              :inline-prompt="field.inlinePrompt"
              :active-icon="field.activeIcon"
              :inactive-icon="field.inactiveIcon"
              :active-text="field.activeText"
              :inactive-text="field.inactiveText"
              :active-value="field.activeValue"
              :inactive-value="field.inactiveValue"
              :active-color="field.activeColor"
              :inactive-color="field.inactiveColor"
              :border-color="field.borderColor"
              :name="field.name"
              :validate-event="field.validateEvent !== false"
              @change="handleFieldChange(field, $event)"
            />

            <!-- 单选框组 -->
            <el-radio-group
              v-else-if="field.type === 'radio'"
              v-model="formData[field.prop]"
              :size="field.size || size"
              :disabled="field.disabled"
              :text-color="field.textColor"
              :fill="field.fill"
              :validate-event="field.validateEvent !== false"
              @change="handleFieldChange(field, $event)"
            >
              <el-radio
                v-for="option in field.options"
                :key="option.value"
                :label="option.value"
                :disabled="option.disabled"
                :border="field.border"
                :size="field.size || size"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>

            <!-- 复选框组 -->
            <el-checkbox-group
              v-else-if="field.type === 'checkbox'"
              v-model="formData[field.prop]"
              :size="field.size || size"
              :disabled="field.disabled"
              :min="field.min"
              :max="field.max"
              :text-color="field.textColor"
              :fill="field.fill"
              :tag="field.tag"
              :validate-event="field.validateEvent !== false"
              @change="handleFieldChange(field, $event)"
            >
              <el-checkbox
                v-for="option in field.options"
                :key="option.value"
                :label="option.value"
                :disabled="option.disabled"
                :border="field.border"
                :size="field.size || size"
                :name="option.name"
                :checked="option.checked"
                :indeterminate="option.indeterminate"
              >
                {{ option.label }}
              </el-checkbox>
            </el-checkbox-group>

            <!-- 滑块 -->
            <el-slider
              v-else-if="field.type === 'slider'"
              v-model="formData[field.prop]"
              :min="field.min || 0"
              :max="field.max || 100"
              :disabled="field.disabled"
              :step="field.step || 1"
              :show-input="field.showInput"
              :show-input-controls="field.showInputControls !== false"
              :size="field.size || size"
              :input-size="field.inputSize"
              :show-stops="field.showStops"
              :show-tooltip="field.showTooltip !== false"
              :format-tooltip="field.formatTooltip"
              :range="field.range"
              :vertical="field.vertical"
              :height="field.height"
              :label="field.sliderLabel"
              :debounce="field.debounce"
              :tooltip-class="field.tooltipClass"
              :marks="field.marks"
              :validate-event="field.validateEvent !== false"
              @change="handleFieldChange(field, $event)"
              @input="handleFieldInput(field, $event)"
            />

            <!-- 评分 -->
            <el-rate
              v-else-if="field.type === 'rate'"
              v-model="formData[field.prop]"
              :max="field.max || 5"
              :disabled="field.disabled"
              :allow-half="field.allowHalf"
              :low-threshold="field.lowThreshold || 2"
              :high-threshold="field.highThreshold || 4"
              :colors="field.colors"
              :void-color="field.voidColor"
              :disabled-void-color="field.disabledVoidColor"
              :icon-classes="field.iconClasses"
              :void-icon-class="field.voidIconClass"
              :disabled-void-icon-class="field.disabledVoidIconClass"
              :show-text="field.showText"
              :show-score="field.showScore"
              :text-color="field.textColor"
              :texts="field.texts"
              :score-template="field.scoreTemplate"
              :size="field.size || size"
              @change="handleFieldChange(field, $event)"
            />

            <!-- 颜色选择器 -->
            <el-color-picker
              v-else-if="field.type === 'color'"
              v-model="formData[field.prop]"
              :disabled="field.disabled"
              :size="field.size || size"
              :show-alpha="field.showAlpha"
              :color-format="field.colorFormat"
              :popper-class="field.popperClass"
              :predefine="field.predefine"
              :validate-event="field.validateEvent !== false"
              @change="handleFieldChange(field, $event)"
              @active-change="handleColorActiveChange(field, $event)"
            />

            <!-- 上传 -->
            <el-upload
              v-else-if="field.type === 'upload'"
              :action="field.action"
              :headers="field.headers"
              :method="field.method || 'post'"
              :multiple="field.multiple"
              :data="field.data"
              :name="field.name || 'file'"
              :with-credentials="field.withCredentials"
              :show-file-list="field.showFileList !== false"
              :drag="field.drag"
              :accept="field.accept"
              :on-preview="field.onPreview"
              :on-remove="
                (file, fileList) => handleUploadRemove(field, file, fileList)
              "
              :on-success="
                (response, file, fileList) =>
                  handleUploadSuccess(field, response, file, fileList)
              "
              :on-error="
                (err, file, fileList) =>
                  handleUploadError(field, err, file, fileList)
              "
              :on-progress="
                (event, file, fileList) =>
                  handleUploadProgress(field, event, file, fileList)
              "
              :on-change="
                (file, fileList) => handleUploadChange(field, file, fileList)
              "
              :before-upload="field.beforeUpload"
              :before-remove="field.beforeRemove"
              :list-type="field.listType"
              :auto-upload="field.autoUpload !== false"
              :file-list="formData[field.prop] || []"
              :http-request="field.httpRequest"
              :disabled="field.disabled"
              :limit="field.limit"
              :on-exceed="field.onExceed"
            >
              <template v-if="field.drag">
                <el-icon class="el-icon--upload">
                  <component :is="field.uploadIcon || 'UploadFilled'" />
                </el-icon>
                <div class="el-upload__text">
                  {{ field.uploadText || "将文件拖到此处，或点击上传" }}
                </div>
                <template v-if="field.uploadTip">
                  <div class="el-upload__tip">{{ field.uploadTip }}</div>
                </template>
              </template>
              <template v-else>
                <el-button :size="field.size || size" type="primary">
                  {{ field.uploadText || "点击上传" }}
                </el-button>
                <template v-if="field.uploadTip">
                  <div class="el-upload__tip">{{ field.uploadTip }}</div>
                </template>
              </template>
            </el-upload>

            <!-- 自定义内容 -->
            <div v-else-if="field.type === 'custom'" class="custom-field">
              <slot
                :name="`custom-${field.prop}`"
                :field="field"
                :value="formData[field.prop]"
                :form-data="formData"
              />
            </div>

            <!-- 表单项帮助文本 -->
            <div v-if="field.help" class="form-field-help">
              {{ field.help }}
            </div>
          </el-form-item>
        </template>
      </el-form>
    </div>

    <!-- 自定义底部 -->
    <template #footer>
      <slot name="footer" :loading="loading" :form-data="formData">
        <div class="dialog-footer">
          <el-button @click="handleCancel" :disabled="loading">
            {{ cancelText }}
          </el-button>
          <el-button type="primary" @click="handleConfirm" :loading="loading">
            {{ confirmText }}
          </el-button>
        </div>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import type {
  FormInstance,
  FormRules,
  FormItemProp,
  AutoFill,
  DatePickerType,
  GetDisabledHours,
  GetDisabledMinutes,
  GetDisabledSeconds,
  UploadFile,
  UploadRawFile,
  UploadFiles,
  UploadUserFile,
  UploadRequestHandler,
  UploadRequestOptions,
} from "element-plus";

// 定义接口
interface FormField {
  prop: string;
  label?: string;
  type?: string;
  placeholder?: string;
  required?: boolean;
  rules?: any[];
  options?: Array<{ label: string; value: any; disabled?: boolean }>;
  disabled?: boolean;
  readonly?: boolean;
  clearable?: boolean;
  size?: "large" | "default" | "small";
  class?: string;
  labelWidth?: string | number;
  showMessage?: boolean;
  inlineMessage?: boolean;
  error?: string;
  help?: string;
  slot?: boolean;
  // 输入框相关
  inputType?: string;
  maxlength?: number;
  minlength?: number;
  showPassword?: boolean;
  showWordLimit?: boolean;
  prefixIcon?: string;
  suffixIcon?: string;
  prepend?: string;
  append?: string;
  resize?: "none" | "both" | "horizontal" | "vertical";
  autosize?: boolean | { minRows?: number; maxRows?: number };
  autocomplete?: string;
  name?: string;
  // 数字输入框相关
  min?: number;
  max?: number;
  step?: number;
  precision?: number;
  controls?: boolean;
  controlsPosition?: "right" | "";
  // 选择器相关
  multiple?: boolean;
  collapseTags?: boolean;
  collapseTagsTooltip?: boolean;
  multipleLimit?: number;
  filterable?: boolean;
  allowCreate?: boolean;
  filterMethod?: Function;
  remote?: boolean;
  remoteMethod?: Function;
  loading?: boolean;
  loadingText?: string;
  noMatchText?: string;
  noDataText?: string;
  popperClass?: string;
  reserveKeyword?: boolean;
  defaultFirstOption?: boolean;
  teleported?: boolean;
  automaticDropdown?: boolean;
  // 级联选择器相关
  showAllLevels?: boolean;
  separator?: string;
  debounce?: number;
  beforeFilter?: Function;
  // 日期选择器相关
  dateType?: string;
  format?: string;
  valueFormat?: string;
  editable?: boolean;
  disabledDate?: Function;
  shortcuts?: any[];
  cellClassName?: Function;
  rangeSeparator?: string;
  startPlaceholder?: string;
  endPlaceholder?: string;
  defaultValue?: Date | [Date, Date];
  defaultTime?: Date | [Date, Date];
  // 时间选择器相关
  isRange?: boolean;
  arrowControl?: boolean;
  disabledHours?: Function;
  disabledMinutes?: Function;
  disabledSeconds?: Function;
  // 开关相关
  width?: number;
  inlinePrompt?: boolean;
  activeIcon?: string;
  inactiveIcon?: string;
  activeText?: string;
  inactiveText?: string;
  activeValue?: boolean | string | number;
  inactiveValue?: boolean | string | number;
  activeColor?: string;
  inactiveColor?: string;
  borderColor?: string;
  validateEvent?: boolean;
  // 单选框/复选框相关
  border?: boolean;
  textColor?: string;
  fill?: string;
  tag?: boolean;
  checked?: boolean;
  indeterminate?: boolean;
  // 滑块相关
  showInput?: boolean;
  showInputControls?: boolean;
  inputSize?: "large" | "default" | "small";
  showStops?: boolean;
  showTooltip?: boolean;
  formatTooltip?: Function;
  range?: boolean;
  vertical?: boolean;
  height?: string;
  sliderLabel?: string;
  tooltipClass?: string;
  marks?: object;
  // 评分相关
  allowHalf?: boolean;
  lowThreshold?: number;
  highThreshold?: number;
  colors?: string[] | object;
  voidColor?: string;
  disabledVoidColor?: string;
  iconClasses?: string[] | object;
  voidIconClass?: string;
  disabledVoidIconClass?: string;
  showText?: boolean;
  showScore?: boolean;
  texts?: string[];
  scoreTemplate?: string;
  // 颜色选择器相关
  showAlpha?: boolean;
  colorFormat?: "hsl" | "hsv" | "hex" | "rgb";
  predefine?: string[];
  // 上传相关
  action?: string;
  headers?: object;
  method?: string;
  data?: object;
  withCredentials?: boolean;
  showFileList?: boolean;
  drag?: boolean;
  accept?: string;
  onPreview?: Function;
  beforeUpload?: Function;
  beforeRemove?: Function;
  listType?: "text" | "picture" | "picture-card";
  autoUpload?: boolean;
  httpRequest?: Function;
  limit?: number;
  onExceed?: Function;
  uploadIcon?: string;
  uploadText?: string;
  uploadTip?: string;
  // 文本域相关
  rows?: number;
}

// Props
interface Props {
  modelValue: boolean;
  title?: string;
  width?: string | number;
  fullscreen?: boolean;
  modal?: boolean;
  modalClass?: string;
  appendToBody?: boolean;
  lockScroll?: boolean;
  customClass?: string;
  openDelay?: number;
  closeDelay?: number;
  closeOnClickModal?: boolean;
  closeOnPressEscape?: boolean;
  showClose?: boolean;
  center?: boolean;
  alignCenter?: boolean;
  destroyOnClose?: boolean;
  fields: FormField[];
  data?: Record<string, any>;
  rules?: FormRules;
  labelWidth?: string | number;
  labelPosition?: "left" | "right" | "top";
  inline?: boolean;
  size?: "large" | "default" | "small";
  disabled?: boolean;
  validateOnRuleChange?: boolean;
  hideRequiredAsterisk?: boolean;
  showMessage?: boolean;
  inlineMessage?: boolean;
  statusIcon?: boolean;
  scrollToError?: boolean;
  loading?: boolean;
  confirmText?: string;
  cancelText?: string;
  beforeClose?: Function;
}

const props = withDefaults(defineProps<Props>(), {
  title: "表单",
  width: "600px",
  fullscreen: false,
  modal: true,
  appendToBody: true,
  lockScroll: true,
  closeOnClickModal: false,
  closeOnPressEscape: true,
  showClose: true,
  center: false,
  alignCenter: true,
  destroyOnClose: false,
  data: () => ({}),
  labelWidth: "100px",
  labelPosition: "right",
  inline: false,
  size: "default",
  disabled: false,
  validateOnRuleChange: true,
  hideRequiredAsterisk: false,
  showMessage: true,
  inlineMessage: false,
  statusIcon: false,
  scrollToError: false,
  loading: false,
  confirmText: "确定",
  cancelText: "取消",
});

// Emits
const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  confirm: [data: Record<string, any>];
  cancel: [];
  open: [];
  opened: [];
  close: [];
  closed: [];
  validate: [prop: string, isValid: boolean, message: string];
  fieldChange: [field: FormField, value: any];
  fieldBlur: [field: FormField, event: Event];
  fieldFocus: [field: FormField, event: Event];
}>();

// 响应式数据
const formRef = ref<FormInstance>();
const formData = ref<Record<string, any>>({});
const formRules = ref<FormRules>({});

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

// 方法
const handleOpen = () => {
  emit("open");
};

const handleOpened = () => {
  emit("opened");
};

const handleClose = () => {
  emit("close");
};

const handleClosed = () => {
  emit("closed");
};

const handleBeforeClose = (done: Function) => {
  if (props.beforeClose) {
    props.beforeClose(done);
  } else {
    done();
  }
};

const handleValidate = (
  prop: FormItemProp,
  isValid: boolean,
  message: string,
) => {
  emit("validate", prop, isValid, message);
};

const handleCancel = () => {
  visible.value = false;
  emit("cancel");
};

const handleConfirm = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    emit("confirm", { ...formData.value });
  } catch (error) {
    ElMessage.error("请检查表单填写是否正确");
  }
};

// 字段事件处理
const handleFieldChange = (field: FormField, value: any) => {
  emit("fieldChange", field, value);
};

const handleFieldBlur = (field: FormField, event: Event) => {
  emit("fieldBlur", field, event);
};

const handleFieldFocus = (field: FormField, event: Event) => {
  emit("fieldFocus", field, event);
};

const handleFieldInput = (field: FormField, value: any) => {
  // 可以在这里处理输入事件
};

const handleFieldClear = (field: FormField) => {
  formData.value[field.prop] = undefined;
};

// 选择器事件处理
const handleSelectVisibleChange = (field: FormField, visible: boolean) => {
  // 处理选择器显示/隐藏
};

const handleSelectRemoveTag = (field: FormField, value: any) => {
  // 处理选择器标签移除
};

// 级联选择器事件处理
const handleCascaderExpandChange = (field: FormField, value: any) => {
  // 处理级联选择器展开变化
};

const handleCascaderVisibleChange = (field: FormField, visible: boolean) => {
  // 处理级联选择器显示/隐藏
};

// 日期选择器事件处理
const handleDateCalendarChange = (field: FormField, value: any) => {
  // 处理日期选择器日历变化
};

const handleDatePanelChange = (field: FormField, value: any) => {
  // 处理日期选择器面板变化
};

const handleDateVisibleChange = (field: FormField, visible: boolean) => {
  // 处理日期选择器显示/隐藏
};

// 时间选择器事件处理
const handleTimeVisibleChange = (field: FormField, visible: boolean) => {
  // 处理时间选择器显示/隐藏
};

// 颜色选择器事件处理
const handleColorActiveChange = (field: FormField, value: any) => {
  // 处理颜色选择器激活变化
};

// 上传事件处理
const handleUploadSuccess = (
  field: FormField,
  response: any,
  file: any,
  fileList: any[],
) => {
  formData.value[field.prop] = fileList;
};

const handleUploadError = (
  field: FormField,
  err: any,
  file: any,
  fileList: any[],
) => {
  ElMessage.error("上传失败");
};

const handleUploadProgress = (
  field: FormField,
  event: any,
  file: any,
  fileList: any[],
) => {
  // 处理上传进度
};

const handleUploadChange = (field: FormField, file: any, fileList: any[]) => {
  formData.value[field.prop] = fileList;
};

const handleUploadRemove = (field: FormField, file: any, fileList: any[]) => {
  formData.value[field.prop] = fileList;
};

// 初始化表单数据
const initFormData = () => {
  const data = { ...props.data };

  props.fields.forEach((field) => {
    if (field.type !== "group" && !(field.prop in data)) {
      // 设置默认值
      switch (field.type) {
        case "checkbox":
          data[field.prop] = [];
          break;
        case "switch":
          data[field.prop] = field.inactiveValue ?? false;
          break;
        case "number":
          data[field.prop] = field.min ?? 0;
          break;
        case "slider":
          data[field.prop] = field.min ?? 0;
          break;
        case "rate":
          data[field.prop] = 0;
          break;
        case "upload":
          data[field.prop] = [];
          break;
        default:
          data[field.prop] = "";
      }
    }
  });

  formData.value = data;
};

// 初始化表单规则
const initFormRules = () => {
  const rules: FormRules = { ...props.rules };

  props.fields.forEach((field) => {
    if (field.type !== "group" && field.rules) {
      rules[field.prop] = field.rules;
    }
  });

  formRules.value = rules;
};

// 监听数据变化
watch(
  () => props.data,
  () => {
    initFormData();
  },
  { deep: true, immediate: true },
);

watch(
  () => props.fields,
  () => {
    initFormData();
    initFormRules();
  },
  { deep: true, immediate: true },
);

watch(
  () => props.rules,
  () => {
    initFormRules();
  },
  { deep: true, immediate: true },
);

// 暴露方法
defineExpose({
  validate: () => formRef.value?.validate(),
  validateField: (props: string | string[]) =>
    formRef.value?.validateField(props),
  resetFields: () => formRef.value?.resetFields(),
  scrollToField: (prop: string) => formRef.value?.scrollToField(prop),
  clearValidate: (props?: string | string[]) =>
    formRef.value?.clearValidate(props),
  getFormData: () => ({ ...formData.value }),
  setFormData: (data: Record<string, any>) => {
    Object.assign(formData.value, data);
  },
});
</script>

<style lang="scss" scoped>
.form-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 4px;

  .form-group-title {
    margin: 20px 0 16px;

    &:first-child {
      margin-top: 0;
    }

    :deep(.el-divider__text) {
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }

  .form-field-help {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    line-height: 1.4;
    margin-top: 4px;
  }

  .custom-field {
    width: 100%;
  }

  // 上传组件样式调整
  :deep(.el-upload) {
    .el-upload__tip {
      margin-top: 8px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  // 表单项间距调整
  :deep(.el-form-item) {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  // 滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: var(--el-fill-color-lighter);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--el-border-color-darker);
    border-radius: 3px;

    &:hover {
      background: var(--el-border-color-dark);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .form-dialog-content {
    max-height: 50vh;
    padding: 0 2px;

    :deep(.el-form-item) {
      margin-bottom: 16px;
    }
  }
}
</style>
