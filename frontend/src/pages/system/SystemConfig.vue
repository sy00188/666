<template>
  <div class="system-config">
    <div class="page-header">
      <h2>系统配置</h2>
      <p>系统参数配置与基础设置管理</p>
      <div class="header-actions">
        <el-button type="primary" @click="exportConfig" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出配置
        </el-button>
        <el-upload
          ref="importUploadRef"
          action="#"
          :before-upload="beforeImportConfig"
          :show-file-list="false"
          accept=".json"
        >
          <el-button type="success">
            <el-icon><Upload /></el-icon>
            导入配置
          </el-button>
        </el-upload>
      </div>
    </div>

    <div class="config-content">
      <el-tabs
        v-model="activeTab"
        type="border-card"
        @tab-change="handleTabChange"
      >
        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>系统基础信息</span>
                <el-button
                  type="primary"
                  size="small"
                  @click="saveBasicSettings"
                  :loading="saving.basic"
                >
                  保存设置
                </el-button>
              </div>
            </template>

            <el-form
              ref="basicFormRef"
              :model="basicSettings"
              :rules="basicRules"
              label-width="150px"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="系统名称" prop="systemName">
                    <el-input
                      v-model="basicSettings.systemName"
                      placeholder="请输入系统名称"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="系统版本" prop="version">
                    <el-input
                      v-model="basicSettings.version"
                      placeholder="请输入系统版本"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="系统Logo" prop="logo">
                    <el-upload
                      class="logo-uploader"
                      action="#"
                      :show-file-list="false"
                      :before-upload="beforeLogoUpload"
                      :on-success="handleLogoSuccess"
                      accept="image/*"
                    >
                      <img
                        v-if="basicSettings.logo"
                        :src="basicSettings.logo"
                        class="logo"
                      />
                      <el-icon v-else class="logo-uploader-icon"
                        ><Plus
                      /></el-icon>
                    </el-upload>
                    <div class="upload-tip">
                      建议尺寸：200x60px，支持 JPG、PNG 格式
                    </div>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="系统图标" prop="favicon">
                    <el-upload
                      class="favicon-uploader"
                      action="#"
                      :show-file-list="false"
                      :before-upload="beforeFaviconUpload"
                      :on-success="handleFaviconSuccess"
                      accept="image/*"
                    >
                      <img
                        v-if="basicSettings.favicon"
                        :src="basicSettings.favicon"
                        class="favicon"
                      />
                      <el-icon v-else class="favicon-uploader-icon"
                        ><Picture
                      /></el-icon>
                    </el-upload>
                    <div class="upload-tip">
                      建议尺寸：32x32px，支持 ICO、PNG 格式
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="系统描述" prop="description">
                <el-input
                  v-model="basicSettings.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入系统描述"
                />
              </el-form-item>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="版权信息" prop="copyright">
                    <el-input
                      v-model="basicSettings.copyright"
                      placeholder="请输入版权信息"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系方式" prop="contact">
                    <el-input
                      v-model="basicSettings.contact"
                      placeholder="请输入联系方式"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="时区设置" prop="timezone">
                    <el-select
                      v-model="basicSettings.timezone"
                      placeholder="请选择时区"
                      filterable
                    >
                      <el-option
                        v-for="tz in timezoneOptions"
                        :key="tz.value"
                        :label="tz.label"
                        :value="tz.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="语言设置" prop="locale">
                    <el-select
                      v-model="basicSettings.locale"
                      placeholder="请选择语言"
                    >
                      <el-option label="简体中文" value="zh-CN" />
                      <el-option label="English" value="en-US" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="主题设置" prop="theme">
                    <el-radio-group v-model="basicSettings.theme">
                      <el-radio value="light">浅色主题</el-radio>
                      <el-radio value="dark">深色主题</el-radio>
                      <el-radio value="auto">跟随系统</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="主色调" prop="primaryColor">
                    <el-color-picker v-model="basicSettings.primaryColor" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="功能开关">
                <el-checkbox v-model="basicSettings.enableRegistration"
                  >允许用户注册</el-checkbox
                >
                <el-checkbox v-model="basicSettings.enableGuestAccess"
                  >允许访客访问</el-checkbox
                >
                <el-checkbox v-model="basicSettings.maintenanceMode"
                  >维护模式</el-checkbox
                >
              </el-form-item>

              <el-form-item
                v-if="basicSettings.maintenanceMode"
                label="维护提示"
                prop="maintenanceMessage"
              >
                <el-input
                  v-model="basicSettings.maintenanceMessage"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入维护提示信息"
                />
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>系统安全配置</span>
                <el-button
                  type="primary"
                  size="small"
                  @click="saveSecuritySettings"
                  :loading="saving.security"
                >
                  保存设置
                </el-button>
              </div>
            </template>

            <el-form
              ref="securityFormRef"
              :model="securitySettings"
              :rules="securityRules"
              label-width="150px"
            >
              <el-divider content-position="left">密码策略</el-divider>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="最小密码长度" prop="minPasswordLength">
                    <el-input-number
                      v-model="securitySettings.minPasswordLength"
                      :min="6"
                      :max="32"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="密码过期天数" prop="passwordExpireDays">
                    <el-input-number
                      v-model="securitySettings.passwordExpireDays"
                      :min="0"
                      :max="365"
                      controls-position="right"
                    />
                    <div class="form-tip">设置为0表示永不过期</div>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="密码复杂度" prop="passwordComplexity">
                <el-checkbox-group
                  v-model="securitySettings.passwordComplexity"
                >
                  <el-checkbox value="lowercase">包含小写字母</el-checkbox>
                  <el-checkbox value="uppercase">包含大写字母</el-checkbox>
                  <el-checkbox value="number">包含数字</el-checkbox>
                  <el-checkbox value="special">包含特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-divider content-position="left">登录安全</el-divider>

              <el-form-item label="登录失败锁定" prop="loginFailLock">
                <el-switch v-model="securitySettings.loginFailLock" />
              </el-form-item>

              <template v-if="securitySettings.loginFailLock">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="最大失败次数" prop="maxLoginFails">
                      <el-input-number
                        v-model="securitySettings.maxLoginFails"
                        :min="3"
                        :max="10"
                        controls-position="right"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="锁定时长(分钟)" prop="lockDuration">
                      <el-input-number
                        v-model="securitySettings.lockDuration"
                        :min="5"
                        :max="1440"
                        controls-position="right"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>

              <el-form-item label="会话超时(分钟)" prop="sessionTimeout">
                <el-input-number
                  v-model="securitySettings.sessionTimeout"
                  :min="30"
                  :max="1440"
                  controls-position="right"
                />
              </el-form-item>

              <el-divider content-position="left">其他安全设置</el-divider>

              <el-form-item label="强制HTTPS" prop="forceHttps">
                <el-switch v-model="securitySettings.forceHttps" />
              </el-form-item>

              <el-form-item label="启用双因子认证" prop="enableTwoFactor">
                <el-switch v-model="securitySettings.enableTwoFactor" />
              </el-form-item>

              <el-form-item label="启用验证码" prop="enableCaptcha">
                <el-switch v-model="securitySettings.enableCaptcha" />
              </el-form-item>

              <el-form-item
                v-if="securitySettings.enableCaptcha"
                label="验证码阈值"
                prop="captchaThreshold"
              >
                <el-input-number
                  v-model="securitySettings.captchaThreshold"
                  :min="1"
                  :max="10"
                  controls-position="right"
                />
                <div class="form-tip">登录失败多少次后显示验证码</div>
              </el-form-item>

              <el-form-item label="IP白名单" prop="ipWhitelist">
                <el-input
                  v-model="securitySettings.ipWhitelist"
                  type="textarea"
                  :rows="3"
                  placeholder="每行一个IP地址或IP段，如：192.168.1.1 或 192.168.1.0/24"
                />
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>

        <!-- 邮件设置 -->
        <el-tab-pane label="邮件设置" name="email">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>邮件服务配置</span>
                <div>
                  <el-button
                    type="info"
                    size="small"
                    @click="testEmailConnection"
                    :loading="testing.email"
                  >
                    测试连接
                  </el-button>
                  <el-button
                    type="primary"
                    size="small"
                    @click="saveEmailSettings"
                    :loading="saving.email"
                  >
                    保存设置
                  </el-button>
                </div>
              </div>
            </template>

            <el-form
              ref="emailFormRef"
              :model="emailSettings"
              :rules="emailRules"
              label-width="150px"
            >
              <el-form-item label="启用邮件服务" prop="enabled">
                <el-switch v-model="emailSettings.enabled" />
              </el-form-item>

              <template v-if="emailSettings.enabled">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="SMTP服务器" prop="host">
                      <el-input
                        v-model="emailSettings.host"
                        placeholder="如：smtp.qq.com"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="端口" prop="port">
                      <el-input-number
                        v-model="emailSettings.port"
                        :min="1"
                        :max="65535"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="用户名" prop="username">
                      <el-input
                        v-model="emailSettings.username"
                        placeholder="邮箱账号"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="密码/授权码" prop="password">
                      <el-input
                        v-model="emailSettings.password"
                        type="password"
                        placeholder="邮箱密码或授权码"
                        show-password
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="发件人名称" prop="fromName">
                      <el-input
                        v-model="emailSettings.fromName"
                        placeholder="发件人显示名称"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="发件人邮箱" prop="fromEmail">
                      <el-input
                        v-model="emailSettings.fromEmail"
                        placeholder="发件人邮箱地址"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="加密方式" prop="encryption">
                  <el-radio-group v-model="emailSettings.encryption">
                    <el-radio value="none">无加密</el-radio>
                    <el-radio value="ssl">SSL</el-radio>
                    <el-radio value="tls">TLS</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="连接超时(秒)" prop="timeout">
                      <el-input-number
                        v-model="emailSettings.timeout"
                        :min="10"
                        :max="300"
                        controls-position="right"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="启用调试" prop="enableDebug">
                      <el-switch v-model="emailSettings.enableDebug" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>
            </el-form>
          </el-card>
        </el-tab-pane>

        <!-- 存储设置 -->
        <el-tab-pane label="存储设置" name="storage">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>文件存储配置</span>
                <div>
                  <el-button
                    type="info"
                    size="small"
                    @click="testStorageConnection"
                    :loading="testing.storage"
                  >
                    测试连接
                  </el-button>
                  <el-button
                    type="primary"
                    size="small"
                    @click="saveStorageSettings"
                    :loading="saving.storage"
                  >
                    保存设置
                  </el-button>
                </div>
              </div>
            </template>

            <el-form
              ref="storageFormRef"
              :model="storageSettings"
              :rules="storageRules"
              label-width="150px"
            >
              <el-form-item label="存储方式" prop="type">
                <el-radio-group v-model="storageSettings.type">
                  <el-radio value="local">本地存储</el-radio>
                  <el-radio value="oss">阿里云OSS</el-radio>
                  <el-radio value="s3">Amazon S3</el-radio>
                  <el-radio value="qiniu">七牛云</el-radio>
                  <el-radio value="cos">腾讯云COS</el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 本地存储配置 -->
              <template v-if="storageSettings.type === 'local'">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="存储路径" prop="localPath">
                      <el-input
                        v-model="storageSettings.localPath"
                        placeholder="/uploads"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="访问域名" prop="localDomain">
                      <el-input
                        v-model="storageSettings.localDomain"
                        placeholder="http://localhost:3000"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
              </template>

              <!-- 云存储配置 -->
              <template v-else>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="AccessKey" prop="accessKey">
                      <el-input
                        v-model="storageSettings.accessKey"
                        placeholder="请输入AccessKey"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="SecretKey" prop="secretKey">
                      <el-input
                        v-model="storageSettings.secretKey"
                        type="password"
                        placeholder="请输入SecretKey"
                        show-password
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="存储桶名称" prop="bucket">
                      <el-input
                        v-model="storageSettings.bucket"
                        placeholder="请输入存储桶名称"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="地域" prop="region">
                      <el-input
                        v-model="storageSettings.region"
                        placeholder="如：oss-cn-hangzhou"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="自定义域名" prop="customDomain">
                  <el-input
                    v-model="storageSettings.customDomain"
                    placeholder="可选，自定义访问域名"
                  />
                </el-form-item>
              </template>

              <el-divider content-position="left">上传限制</el-divider>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="最大文件大小(MB)" prop="maxFileSize">
                    <el-input-number
                      v-model="storageSettings.maxFileSize"
                      :min="1"
                      :max="1024"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="启用压缩" prop="enableCompression">
                    <el-switch v-model="storageSettings.enableCompression" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="允许的文件类型" prop="allowedTypes">
                <el-input
                  v-model="storageSettings.allowedTypes"
                  placeholder="用逗号分隔，如：jpg,png,pdf,doc"
                />
              </el-form-item>

              <el-form-item label="启用水印" prop="enableWatermark">
                <el-switch v-model="storageSettings.enableWatermark" />
              </el-form-item>

              <template v-if="storageSettings.enableWatermark">
                <el-form-item label="水印文字" prop="watermarkText">
                  <el-input
                    v-model="storageSettings.watermarkText"
                    placeholder="水印文字内容"
                  />
                </el-form-item>
              </template>
            </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type TabPaneName,
} from "element-plus";
import { Plus, Picture, Download, Upload } from "@element-plus/icons-vue";
import { systemApi } from "@/api/modules/system";
import type {
  SystemSettings,
  SecuritySettings,
  EmailSettings,
  StorageSettings,
} from "@/types/system";

// 响应式数据
const activeTab = ref("basic");
const exporting = ref(false);

// 表单引用
const basicFormRef = ref<FormInstance>();
const securityFormRef = ref<FormInstance>();
const emailFormRef = ref<FormInstance>();
const storageFormRef = ref<FormInstance>();
const importUploadRef = ref();

// 保存状态
const saving = reactive({
  basic: false,
  security: false,
  email: false,
  storage: false,
});

// 测试状态
const testing = reactive({
  email: false,
  storage: false,
});

// 基础设置
const basicSettings = reactive<SystemSettings>({
  systemName: "档案管理系统",
  version: "1.0.0",
  logo: "",
  favicon: "",
  description: "企业档案数字化管理平台",
  copyright: "© 2024 档案管理系统 版权所有",
  contact: "admin@example.com",
  timezone: "Asia/Shanghai",
  locale: "zh-CN",
  dateFormat: "YYYY-MM-DD",
  timeFormat: "HH:mm:ss",
  theme: "light",
  primaryColor: "#409EFF",
  enableRegistration: false,
  enableGuestAccess: false,
  maintenanceMode: false,
  maintenanceMessage: "",
});

// 安全设置
const securitySettings = reactive<SecuritySettings>({
  minPasswordLength: 8,
  passwordComplexity: ["lowercase", "number"],
  passwordExpireDays: 90,
  loginFailLock: true,
  maxLoginFails: 5,
  lockDuration: 30,
  sessionTimeout: 120,
  forceHttps: false,
  ipWhitelist: "",
  enableTwoFactor: false,
  enableCaptcha: false,
  captchaThreshold: 3,
  enableAuditLog: true,
  logRetentionDays: 90,
});

// 邮件设置
const emailSettings = reactive<EmailSettings>({
  enabled: false,
  host: "",
  port: 587,
  username: "",
  password: "",
  fromName: "",
  fromEmail: "",
  encryption: "tls",
  timeout: 30,
  enableAuth: true,
  enableDebug: false,
});

// 存储设置
const storageSettings = reactive<StorageSettings>({
  type: "local",
  localPath: "/uploads",
  localDomain: "http://localhost:3000",
  accessKey: "",
  secretKey: "",
  bucket: "",
  region: "",
  customDomain: "",
  maxFileSize: 10,
  allowedTypes: "jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,txt",
  enableCompression: false,
  compressionQuality: 80,
  enableWatermark: false,
  watermarkText: "",
  watermarkImage: "",
});

// 时区选项
const timezoneOptions = [
  { label: "北京时间 (UTC+8)", value: "Asia/Shanghai" },
  { label: "东京时间 (UTC+9)", value: "Asia/Tokyo" },
  { label: "纽约时间 (UTC-5)", value: "America/New_York" },
  { label: "伦敦时间 (UTC+0)", value: "Europe/London" },
  { label: "巴黎时间 (UTC+1)", value: "Europe/Paris" },
];

// 表单验证规则
const basicRules = {
  systemName: [{ required: true, message: "请输入系统名称", trigger: "blur" }],
  version: [{ required: true, message: "请输入系统版本", trigger: "blur" }],
};

const securityRules = {
  minPasswordLength: [
    { required: true, message: "请设置密码最小长度", trigger: "blur" },
  ],
};

const emailRules = {
  host: [{ required: true, message: "请输入SMTP服务器", trigger: "blur" }],
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const storageRules = {
  localPath: [{ required: true, message: "请输入存储路径", trigger: "blur" }],
  accessKey: [{ required: true, message: "请输入AccessKey", trigger: "blur" }],
  secretKey: [{ required: true, message: "请输入SecretKey", trigger: "blur" }],
  bucket: [{ required: true, message: "请输入存储桶名称", trigger: "blur" }],
};

// 方法
const loadSettings = async () => {
  try {
    // 加载基础设置
    const basicRes = await systemApi.getSystemSettings();
    if (basicRes.success) {
      Object.assign(basicSettings, basicRes.data);
    }

    // 加载安全设置
    const securityRes = await systemApi.getSecuritySettings();
    if (securityRes.success) {
      Object.assign(securitySettings, securityRes.data);
    }

    // 加载邮件设置
    const emailRes = await systemApi.getEmailSettings();
    if (emailRes.success) {
      Object.assign(emailSettings, emailRes.data);
    }

    // 加载存储设置
    const storageRes = await systemApi.getStorageSettings();
    if (storageRes.success) {
      Object.assign(storageSettings, storageRes.data);
    }
  } catch (error) {
    console.error("加载设置失败:", error);
    ElMessage.error("加载设置失败");
  }
};

const saveBasicSettings = async () => {
  if (!basicFormRef.value) return;

  try {
    await basicFormRef.value.validate();
    saving.basic = true;

    const res = await systemApi.updateSystemSettings(basicSettings);
    if (res.success) {
      ElMessage.success("基础设置保存成功");
    } else {
      ElMessage.error(res.message || "保存失败");
    }
  } catch (error) {
    console.error("保存基础设置失败:", error);
    ElMessage.error("保存失败");
  } finally {
    saving.basic = false;
  }
};

const saveSecuritySettings = async () => {
  if (!securityFormRef.value) return;

  try {
    await securityFormRef.value.validate();
    saving.security = true;

    const res = await systemApi.updateSecuritySettings(securitySettings);
    if (res.success) {
      ElMessage.success("安全设置保存成功");
    } else {
      ElMessage.error(res.message || "保存失败");
    }
  } catch (error) {
    console.error("保存安全设置失败:", error);
    ElMessage.error("保存失败");
  } finally {
    saving.security = false;
  }
};

const saveEmailSettings = async () => {
  if (!emailFormRef.value) return;

  try {
    await emailFormRef.value.validate();
    saving.email = true;

    const res = await systemApi.updateEmailSettings(emailSettings);
    if (res.success) {
      ElMessage.success("邮件设置保存成功");
    } else {
      ElMessage.error(res.message || "保存失败");
    }
  } catch (error) {
    console.error("保存邮件设置失败:", error);
    ElMessage.error("保存失败");
  } finally {
    saving.email = false;
  }
};

const saveStorageSettings = async () => {
  if (!storageFormRef.value) return;

  try {
    await storageFormRef.value.validate();
    saving.storage = true;

    const res = await systemApi.updateStorageSettings(storageSettings);
    if (res.success) {
      ElMessage.success("存储设置保存成功");
    } else {
      ElMessage.error(res.message || "保存失败");
    }
  } catch (error) {
    console.error("保存存储设置失败:", error);
    ElMessage.error("保存失败");
  } finally {
    saving.storage = false;
  }
};

const testEmailConnection = async () => {
  testing.email = true;
  try {
    const res = await systemApi.testEmailConnection();
    if (res.success) {
      ElMessage.success("邮件连接测试成功");
    } else {
      ElMessage.error(res.data?.message || "连接测试失败");
    }
  } catch (error) {
    console.error("邮件连接测试失败:", error);
    ElMessage.error("连接测试失败");
  } finally {
    testing.email = false;
  }
};

const testStorageConnection = async () => {
  testing.storage = true;
  try {
    const res = await systemApi.testStorageConnection();
    if (res.success) {
      ElMessage.success("存储连接测试成功");
    } else {
      ElMessage.error(res.data?.message || "连接测试失败");
    }
  } catch (error) {
    console.error("存储连接测试失败:", error);
    ElMessage.error("连接测试失败");
  } finally {
    testing.storage = false;
  }
};

const beforeLogoUpload = (file: File) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
    return false;
  }

  // 上传Logo
  systemApi
    .uploadLogo(file)
    .then((res) => {
      if (res.success) {
        basicSettings.logo = res.data.url;
        ElMessage.success("Logo上传成功");
      } else {
        ElMessage.error("Logo上传失败");
      }
    })
    .catch(() => {
      ElMessage.error("Logo上传失败");
    });

  return false;
};

const handleLogoSuccess = () => {
  // 由beforeLogoUpload处理
};

const beforeFaviconUpload = (file: File) => {
  const isImage = file.type.startsWith("image/");
  const isLt1M = file.size / 1024 / 1024 < 1;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false;
  }
  if (!isLt1M) {
    ElMessage.error("图片大小不能超过 1MB!");
    return false;
  }

  // 上传Favicon
  systemApi
    .uploadFavicon(file)
    .then((res) => {
      if (res.success) {
        basicSettings.favicon = res.data.url;
        ElMessage.success("图标上传成功");
      } else {
        ElMessage.error("图标上传失败");
      }
    })
    .catch(() => {
      ElMessage.error("图标上传失败");
    });

  return false;
};

const handleFaviconSuccess = () => {
  // 由beforeFaviconUpload处理
};

const exportConfig = async () => {
  exporting.value = true;
  try {
    const res = await systemApi.exportSystemConfig();
    if (res.success) {
      // 下载文件
      const link = document.createElement("a");
      link.href = res.data.url;
      link.download = `system-config-${new Date().toISOString().split("T")[0]}.json`;
      link.click();
      ElMessage.success("配置导出成功");
    } else {
      ElMessage.error("配置导出失败");
    }
  } catch (error) {
    console.error("导出配置失败:", error);
    ElMessage.error("配置导出失败");
  } finally {
    exporting.value = false;
  }
};

const beforeImportConfig = (file: File) => {
  const isJSON =
    file.type === "application/json" || file.name.endsWith(".json");

  if (!isJSON) {
    ElMessage.error("只能上传JSON格式的配置文件!");
    return false;
  }

  ElMessageBox.confirm("导入配置将覆盖当前设置，是否继续？", "确认导入", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      systemApi
        .importSystemConfig(file)
        .then((res) => {
          if (res.success) {
            ElMessage.success("配置导入成功，请刷新页面查看最新设置");
            setTimeout(() => {
              location.reload();
            }, 2000);
          } else {
            ElMessage.error("配置导入失败");
          }
        })
        .catch(() => {
          ElMessage.error("配置导入失败");
        });
    })
    .catch(() => {
      // 用户取消
    });

  return false;
};

const handleTabChange = (tabName: TabPaneName) => {
  activeTab.value = tabName as string;
};

// 生命周期
onMounted(() => {
  loadSettings();
});
</script>

<style scoped lang="scss">
.system-config {
  padding: $spacing-lg;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-lg;

    h2 {
      margin: 0 0 $spacing-xs 0;
      color: $text-primary;
      font-size: 24px;
      font-weight: 600;
    }

    p {
      margin: 0;
      color: $text-secondary;
      font-size: 14px;
    }

    .header-actions {
      display: flex;
      gap: $spacing-sm;
    }
  }

  .config-content {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        font-weight: 600;
        color: $text-primary;
      }
    }
  }
}

.logo-uploader {
  :deep(.el-upload) {
    border: 1px dashed $border-color;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    width: 200px;
    height: 60px;

    &:hover {
      border-color: $primary-color;
    }
  }

  .logo {
    width: 200px;
    height: 60px;
    object-fit: contain;
    display: block;
  }

  .logo-uploader-icon {
    font-size: 28px;
    color: $text-placeholder;
    width: 200px;
    height: 60px;
    line-height: 60px;
    text-align: center;
  }
}

.favicon-uploader {
  :deep(.el-upload) {
    border: 1px dashed $border-color;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    width: 48px;
    height: 48px;

    &:hover {
      border-color: $primary-color;
    }
  }

  .favicon {
    width: 48px;
    height: 48px;
    object-fit: contain;
    display: block;
  }

  .favicon-uploader-icon {
    font-size: 20px;
    color: $text-placeholder;
    width: 48px;
    height: 48px;
    line-height: 48px;
    text-align: center;
  }
}

.upload-tip {
  font-size: 12px;
  color: $text-secondary;
  margin-top: $spacing-xs;
}

.form-tip {
  font-size: 12px;
  color: $text-secondary;
  margin-top: $spacing-xs;
}

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-card) {
  border: none;
  box-shadow: $box-shadow-light;
}

:deep(.el-form-item) {
  margin-bottom: $spacing-lg;
}

:deep(.el-checkbox-group) {
  .el-checkbox {
    margin-right: $spacing-lg;
    margin-bottom: $spacing-sm;
  }
}

:deep(.el-divider) {
  margin: $spacing-xl 0 $spacing-lg 0;
}
</style>
