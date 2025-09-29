<template>
  <div class="system-settings">
    <div class="page-header">
      <h2>系统设置</h2>
      <p>系统参数配置与基础设置管理</p>
    </div>

    <div class="settings-content">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础设置 -->
        <el-tab-pane label="基础设置" name="basic">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>系统基础信息</span>
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
                    >
                      <img
                        v-if="basicSettings.favicon"
                        :src="basicSettings.favicon"
                        class="favicon"
                      />
                      <el-icon v-else class="favicon-uploader-icon"
                        ><Plus
                      /></el-icon>
                    </el-upload>
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

              <el-form-item>
                <el-button
                  type="primary"
                  @click="saveBasicSettings"
                  :loading="saving"
                >
                  保存设置
                </el-button>
                <el-button @click="resetBasicSettings">重置</el-button>
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
              </div>
            </template>

            <el-form
              ref="securityFormRef"
              :model="securitySettings"
              :rules="securityRules"
              label-width="180px"
            >
              <el-form-item label="密码最小长度" prop="minPasswordLength">
                <el-input-number
                  v-model="securitySettings.minPasswordLength"
                  :min="6"
                  :max="20"
                />
                <span style="margin-left: 8px; color: #999">位</span>
              </el-form-item>

              <el-form-item label="密码复杂度要求" prop="passwordComplexity">
                <el-checkbox-group
                  v-model="securitySettings.passwordComplexity"
                >
                  <el-checkbox value="uppercase">包含大写字母</el-checkbox>
                  <el-checkbox value="lowercase">包含小写字母</el-checkbox>
                  <el-checkbox value="number">包含数字</el-checkbox>
                  <el-checkbox value="special">包含特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item label="密码有效期" prop="passwordExpireDays">
                <el-input-number
                  v-model="securitySettings.passwordExpireDays"
                  :min="0"
                  :max="365"
                />
                <span style="margin-left: 8px; color: #999"
                  >天（0表示永不过期）</span
                >
              </el-form-item>

              <el-form-item label="登录失败锁定" prop="loginFailLock">
                <el-switch v-model="securitySettings.loginFailLock" />
              </el-form-item>

              <el-form-item
                label="最大失败次数"
                prop="maxLoginFails"
                v-if="securitySettings.loginFailLock"
              >
                <el-input-number
                  v-model="securitySettings.maxLoginFails"
                  :min="3"
                  :max="10"
                />
                <span style="margin-left: 8px; color: #999">次</span>
              </el-form-item>

              <el-form-item
                label="锁定时长"
                prop="lockDuration"
                v-if="securitySettings.loginFailLock"
              >
                <el-input-number
                  v-model="securitySettings.lockDuration"
                  :min="5"
                  :max="1440"
                />
                <span style="margin-left: 8px; color: #999">分钟</span>
              </el-form-item>

              <el-form-item label="会话超时" prop="sessionTimeout">
                <el-input-number
                  v-model="securitySettings.sessionTimeout"
                  :min="30"
                  :max="1440"
                />
                <span style="margin-left: 8px; color: #999">分钟</span>
              </el-form-item>

              <el-form-item label="强制HTTPS" prop="forceHttps">
                <el-switch v-model="securitySettings.forceHttps" />
              </el-form-item>

              <el-form-item label="IP白名单" prop="ipWhitelist">
                <el-input
                  v-model="securitySettings.ipWhitelist"
                  type="textarea"
                  :rows="3"
                  placeholder="每行一个IP地址或IP段，如：192.168.1.1 或 192.168.1.0/24"
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  @click="saveSecuritySettings"
                  :loading="saving"
                >
                  保存设置
                </el-button>
                <el-button @click="resetSecuritySettings">重置</el-button>
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
                <el-button
                  type="primary"
                  size="small"
                  @click="testEmailConnection"
                >
                  测试连接
                </el-button>
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
              </template>

              <el-form-item>
                <el-button
                  type="primary"
                  @click="saveEmailSettings"
                  :loading="saving"
                >
                  保存设置
                </el-button>
                <el-button @click="resetEmailSettings">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>

        <!-- 存储设置 -->
        <el-tab-pane label="存储设置" name="storage">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>文件存储配置</span>
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
                  <el-radio value="cos">腾讯云COS</el-radio>
                  <el-radio value="qiniu">七牛云</el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 本地存储配置 -->
              <template v-if="storageSettings.type === 'local'">
                <el-form-item label="存储路径" prop="localPath">
                  <el-input
                    v-model="storageSettings.localPath"
                    placeholder="文件存储路径"
                  />
                </el-form-item>
                <el-form-item label="访问域名" prop="localDomain">
                  <el-input
                    v-model="storageSettings.localDomain"
                    placeholder="文件访问域名"
                  />
                </el-form-item>
              </template>

              <!-- 云存储配置 -->
              <template v-if="storageSettings.type !== 'local'">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="AccessKey" prop="accessKey">
                      <el-input
                        v-model="storageSettings.accessKey"
                        placeholder="访问密钥ID"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="SecretKey" prop="secretKey">
                      <el-input
                        v-model="storageSettings.secretKey"
                        type="password"
                        placeholder="访问密钥Secret"
                        show-password
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="存储桶" prop="bucket">
                      <el-input
                        v-model="storageSettings.bucket"
                        placeholder="存储桶名称"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="地域" prop="region">
                      <el-input
                        v-model="storageSettings.region"
                        placeholder="存储地域"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="自定义域名" prop="customDomain">
                  <el-input
                    v-model="storageSettings.customDomain"
                    placeholder="自定义访问域名（可选）"
                  />
                </el-form-item>
              </template>

              <el-form-item label="最大文件大小" prop="maxFileSize">
                <el-input-number
                  v-model="storageSettings.maxFileSize"
                  :min="1"
                  :max="1024"
                />
                <span style="margin-left: 8px; color: #999">MB</span>
              </el-form-item>

              <el-form-item label="允许的文件类型" prop="allowedTypes">
                <el-input
                  v-model="storageSettings.allowedTypes"
                  placeholder="如：jpg,png,pdf,doc,docx"
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  @click="saveStorageSettings"
                  :loading="saving"
                >
                  保存设置
                </el-button>
                <el-button @click="resetStorageSettings">重置</el-button>
                <el-button
                  @click="testStorageConnection"
                  v-if="storageSettings.type !== 'local'"
                >
                  测试连接
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";

const activeTab = ref("basic");
const saving = ref(false);

// 表单引用
const basicFormRef = ref();
const securityFormRef = ref();
const emailFormRef = ref();
const storageFormRef = ref();

// 基础设置
const basicSettings = reactive({
  systemName: "档案管理系统",
  version: "1.0.0",
  logo: "",
  favicon: "",
  description: "企业档案数字化管理平台",
  copyright: "© 2024 档案管理系统 版权所有",
  contact: "admin@example.com",
});

const basicRules = {
  systemName: [{ required: true, message: "请输入系统名称", trigger: "blur" }],
  version: [{ required: true, message: "请输入系统版本", trigger: "blur" }],
};

// 安全设置
const securitySettings = reactive({
  minPasswordLength: 8,
  passwordComplexity: ["lowercase", "number"],
  passwordExpireDays: 90,
  loginFailLock: true,
  maxLoginFails: 5,
  lockDuration: 30,
  sessionTimeout: 120,
  forceHttps: false,
  ipWhitelist: "",
});

const securityRules = {
  minPasswordLength: [
    { required: true, message: "请设置密码最小长度", trigger: "blur" },
  ],
};

// 邮件设置
const emailSettings = reactive({
  enabled: false,
  host: "",
  port: 587,
  username: "",
  password: "",
  fromName: "",
  fromEmail: "",
  encryption: "tls",
});

const emailRules = {
  host: [{ required: true, message: "请输入SMTP服务器", trigger: "blur" }],
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

// 存储设置
const storageSettings = reactive({
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
});

const storageRules = {
  localPath: [{ required: true, message: "请输入存储路径", trigger: "blur" }],
  accessKey: [{ required: true, message: "请输入AccessKey", trigger: "blur" }],
  secretKey: [{ required: true, message: "请输入SecretKey", trigger: "blur" }],
  bucket: [{ required: true, message: "请输入存储桶名称", trigger: "blur" }],
};

// 方法
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
  return true;
};

const handleLogoSuccess = (response: any, file: any) => {
  basicSettings.logo = URL.createObjectURL(file.raw);
};

const beforeFaviconUpload = (file: File) => {
  const isImage = file.type.startsWith("image/");
  const isLt1M = file.size / 1024 / 1024 < 1;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false;
  }
  if (!isLt1M) {
    ElMessage.error("图标大小不能超过 1MB!");
    return false;
  }
  return true;
};

const handleFaviconSuccess = (response: any, file: any) => {
  basicSettings.favicon = URL.createObjectURL(file.raw);
};

const saveBasicSettings = async () => {
  try {
    await basicFormRef.value.validate();
    saving.value = true;

    // 保存逻辑
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success("基础设置保存成功");
  } catch (error) {
    console.error("保存失败:", error);
  } finally {
    saving.value = false;
  }
};

const resetBasicSettings = () => {
  basicFormRef.value.resetFields();
};

const saveSecuritySettings = async () => {
  try {
    await securityFormRef.value.validate();
    saving.value = true;

    // 保存逻辑
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success("安全设置保存成功");
  } catch (error) {
    console.error("保存失败:", error);
  } finally {
    saving.value = false;
  }
};

const resetSecuritySettings = () => {
  securityFormRef.value.resetFields();
};

const saveEmailSettings = async () => {
  try {
    if (emailSettings.enabled) {
      await emailFormRef.value.validate();
    }
    saving.value = true;

    // 保存逻辑
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success("邮件设置保存成功");
  } catch (error) {
    console.error("保存失败:", error);
  } finally {
    saving.value = false;
  }
};

const resetEmailSettings = () => {
  emailFormRef.value.resetFields();
};

const testEmailConnection = async () => {
  try {
    if (!emailSettings.enabled) {
      ElMessage.warning("请先启用邮件服务");
      return;
    }

    await emailFormRef.value.validate();

    // 测试连接逻辑
    ElMessage.success("邮件服务连接测试成功");
  } catch (error) {
    ElMessage.error("邮件服务连接测试失败");
  }
};

const saveStorageSettings = async () => {
  try {
    await storageFormRef.value.validate();
    saving.value = true;

    // 保存逻辑
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success("存储设置保存成功");
  } catch (error) {
    console.error("保存失败:", error);
  } finally {
    saving.value = false;
  }
};

const resetStorageSettings = () => {
  storageFormRef.value.resetFields();
};

const testStorageConnection = async () => {
  try {
    await storageFormRef.value.validate();

    // 测试连接逻辑
    ElMessage.success("存储服务连接测试成功");
  } catch (error) {
    ElMessage.error("存储服务连接测试失败");
  }
};
</script>

<style lang="scss" scoped>
.system-settings {
  padding: $spacing-lg;
}

.page-header {
  margin-bottom: $spacing-lg;

  h2 {
    margin: 0 0 $spacing-sm 0;
    color: $text-primary;
  }

  p {
    margin: 0;
    color: $text-secondary;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-uploader,
.favicon-uploader {
  :deep(.el-upload) {
    border: 1px dashed $border-light;
    border-radius: $border-radius;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;

    &:hover {
      border-color: $color-primary;
    }
  }
}

.logo-uploader {
  :deep(.el-upload) {
    width: 120px;
    height: 60px;
  }

  .logo {
    width: 120px;
    height: 60px;
    object-fit: contain;
    display: block;
  }

  .logo-uploader-icon {
    font-size: 28px;
    color: $text-placeholder;
    width: 120px;
    height: 60px;
    line-height: 60px;
    text-align: center;
  }
}

.favicon-uploader {
  :deep(.el-upload) {
    width: 48px;
    height: 48px;
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

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-card) {
  border: none;
  box-shadow: none;
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
</style>
