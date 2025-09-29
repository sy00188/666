import { request } from "@/utils/request";
import type {
  Archive,
  ArchiveSearchParams,
  ArchiveCreateRequest,
  ArchiveUpdateRequest,
  ArchiveCategory,
  ArchiveStats,
  ApiResponse,
} from "@/types/archive";

export const archiveApi = {
  // 获取档案列表
  getArchiveList(params: ArchiveSearchParams): Promise<
    ApiResponse<{
      list: Archive[];
      total: number;
      page: number;
      size: number;
    }>
  > {
    return request.get("/v1/archives", { params });
  },

  // 获取档案详情
  getArchiveDetail(id: number): Promise<ApiResponse<Archive>> {
    return request.get(`/v1/archives/${id}`);
  },

  // 创建档案
  createArchive(data: ArchiveCreateRequest): Promise<ApiResponse<Archive>> {
    return request.post("/v1/archives", data);
  },

  // 更新档案
  updateArchive(
    id: number,
    data: ArchiveUpdateRequest,
  ): Promise<ApiResponse<Archive>> {
    return request.put(`/v1/archives/${id}`, data);
  },

  // 删除档案
  deleteArchive(id: number): Promise<ApiResponse<null>> {
    return request.delete(`/v1/archives/${id}`);
  },

  // 批量删除档案
  batchDeleteArchives(ids: number[]): Promise<ApiResponse<null>> {
    return request.post("/v1/archives/batch-delete", { archiveIds: ids });
  },

  // 获取档案分类
  getArchiveCategories(): Promise<ApiResponse<ArchiveCategory[]>> {
    return request.get("/v1/categories");
  },

  // 创建档案分类
  createArchiveCategory(
    data: Omit<ArchiveCategory, "categoryId" | "createdAt" | "updatedAt">,
  ): Promise<ApiResponse<ArchiveCategory>> {
    return request.post("/v1/categories", data);
  },

  // 更新档案分类
  updateArchiveCategory(
    id: number,
    data: Partial<ArchiveCategory>,
  ): Promise<ApiResponse<ArchiveCategory>> {
    return request.put(`/v1/categories/${id}`, data);
  },

  // 删除档案分类
  deleteArchiveCategory(id: number): Promise<ApiResponse<null>> {
    return request.delete(`/v1/categories/${id}`);
  },

  // 档案统计
  getArchiveStatistics(): Promise<ApiResponse<ArchiveStats>> {
    return request.get("/v1/archives/statistics");
  },

  // 文件上传
  uploadArchiveFiles(
    archiveId: number,
    files: File[],
  ): Promise<ApiResponse<{ fileIds: number[] }>> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append("files", file);
    });
    formData.append("archiveId", archiveId.toString());

    return request.post("/v1/archives/upload", formData);
  },

  // 文件下载
  downloadArchiveFile(archiveId: number, fileId: number): Promise<Blob> {
    return request.get(`/v1/archives/${archiveId}/files/${fileId}/download`, {
      responseType: "blob",
    });
  },

  // 文件预览
  previewArchiveFile(archiveId: number, fileId: number): Promise<Blob> {
    return request.get(`/v1/archives/${archiveId}/files/${fileId}/preview`, {
      responseType: "blob",
    });
  },

  // 导出档案
  exportArchives(params: ArchiveSearchParams): Promise<Blob> {
    return request.get("/v1/archives/export", {
      params,
      responseType: "blob",
    });
  },

  // 搜索档案
  searchArchives(
    params: {
      keyword: string;
      searchType?: "title" | "content" | "all";
      categoryId?: number;
      securityLevel?: string;
    } & Partial<ArchiveSearchParams>,
  ): Promise<
    ApiResponse<{
      list: Archive[];
      total: number;
      page: number;
      size: number;
    }>
  > {
    return request.get("/v1/archives/search", { params });
  },

  // 获取档案操作日志
  getArchiveLogs(archiveId: number): Promise<ApiResponse<any[]>> {
    return request.get(`/v1/archives/${archiveId}/logs`);
  },

  // 档案审核
  reviewArchive(
    id: number,
    data: {
      action: "approve" | "reject";
      remark?: string;
    },
  ): Promise<ApiResponse<null>> {
    return request.post(`/v1/archives/${id}/review`, data);
  },

  // 档案归档
  archiveDocument(id: number): Promise<ApiResponse<null>> {
    return request.post(`/v1/archives/${id}/archive`);
  },

  // 档案解除归档
  unarchiveDocument(id: number): Promise<ApiResponse<null>> {
    return request.post(`/v1/archives/${id}/unarchive`);
  },

  // 生成档案报告
  generateArchiveReport(_params: ArchiveSearchParams): Promise<Blob> {
    return request.get("/v1/archives/report", {
      responseType: "blob",
    });
  },
};
