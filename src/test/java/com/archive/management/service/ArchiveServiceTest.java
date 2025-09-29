package com.archive.management.service;

import com.archive.management.dto.ArchiveDTO;
import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.enums.ArchiveStatus;
import com.archive.management.enums.ArchiveType;
import com.archive.management.exception.ArchiveNotFoundException;
import com.archive.management.exception.BusinessException;
import com.archive.management.repository.ArchiveRepository;
import com.archive.management.repository.UserRepository;
import com.archive.management.service.impl.ArchiveServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 档案服务单元测试类
 * 测试档案业务逻辑的各种场景
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("档案服务测试")
class ArchiveServiceTest {

    @Mock
    private ArchiveRepository archiveRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArchiveServiceImpl archiveService;

    private Archive testArchive;
    private ArchiveDTO testArchiveDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testArchive = new Archive();
        testArchive.setId(1L);
        testArchive.setTitle("测试档案");
        testArchive.setDescription("这是一个测试档案");
        testArchive.setType(ArchiveType.DOCUMENT);
        testArchive.setStatus(ArchiveStatus.ACTIVE);
        testArchive.setCreatedBy(testUser);
        testArchive.setCreatedAt(LocalDateTime.now());
        testArchive.setUpdatedAt(LocalDateTime.now());

        testArchiveDTO = new ArchiveDTO();
        testArchiveDTO.setId(1L);
        testArchiveDTO.setTitle("测试档案");
        testArchiveDTO.setDescription("这是一个测试档案");
        testArchiveDTO.setType(ArchiveType.DOCUMENT);
        testArchiveDTO.setStatus(ArchiveStatus.ACTIVE);
        testArchiveDTO.setCreatedBy(1L);
    }

    @Test
    @DisplayName("创建档案 - 成功")
    void createArchive_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(archiveRepository.save(any(Archive.class))).thenReturn(testArchive);

        // When
        ArchiveDTO result = archiveService.createArchive(testArchiveDTO);

        // Then
        assertNotNull(result);
        assertEquals(testArchiveDTO.getTitle(), result.getTitle());
        assertEquals(testArchiveDTO.getDescription(), result.getDescription());
        assertEquals(testArchiveDTO.getType(), result.getType());
        assertEquals(testArchiveDTO.getStatus(), result.getStatus());

        verify(userRepository).findById(1L);
        verify(archiveRepository).save(any(Archive.class));
    }

    @Test
    @DisplayName("创建档案 - 用户不存在")
    void createArchive_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> {
            archiveService.createArchive(testArchiveDTO);
        });

        verify(userRepository).findById(1L);
        verify(archiveRepository, never()).save(any(Archive.class));
    }

    @Test
    @DisplayName("根据ID获取档案 - 成功")
    void getArchiveById_Success() {
        // Given
        when(archiveRepository.findById(1L)).thenReturn(Optional.of(testArchive));

        // When
        ArchiveDTO result = archiveService.getArchiveById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testArchive.getId(), result.getId());
        assertEquals(testArchive.getTitle(), result.getTitle());
        assertEquals(testArchive.getDescription(), result.getDescription());

        verify(archiveRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取档案 - 档案不存在")
    void getArchiveById_NotFound() {
        // Given
        when(archiveRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ArchiveNotFoundException.class, () -> {
            archiveService.getArchiveById(1L);
        });

        verify(archiveRepository).findById(1L);
    }

    @Test
    @DisplayName("更新档案 - 成功")
    void updateArchive_Success() {
        // Given
        ArchiveDTO updateDTO = new ArchiveDTO();
        updateDTO.setId(1L);
        updateDTO.setTitle("更新后的标题");
        updateDTO.setDescription("更新后的描述");
        updateDTO.setType(ArchiveType.IMAGE);
        updateDTO.setStatus(ArchiveStatus.ARCHIVED);

        when(archiveRepository.findById(1L)).thenReturn(Optional.of(testArchive));
        when(archiveRepository.save(any(Archive.class))).thenReturn(testArchive);

        // When
        ArchiveDTO result = archiveService.updateArchive(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(archiveRepository).findById(1L);
        verify(archiveRepository).save(any(Archive.class));
    }

    @Test
    @DisplayName("更新档案 - 档案不存在")
    void updateArchive_NotFound() {
        // Given
        ArchiveDTO updateDTO = new ArchiveDTO();
        updateDTO.setTitle("更新后的标题");

        when(archiveRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ArchiveNotFoundException.class, () -> {
            archiveService.updateArchive(1L, updateDTO);
        });

        verify(archiveRepository).findById(1L);
        verify(archiveRepository, never()).save(any(Archive.class));
    }

    @Test
    @DisplayName("删除档案 - 成功")
    void deleteArchive_Success() {
        // Given
        when(archiveRepository.findById(1L)).thenReturn(Optional.of(testArchive));
        doNothing().when(archiveRepository).delete(testArchive);

        // When
        assertDoesNotThrow(() -> {
            archiveService.deleteArchive(1L);
        });

        // Then
        verify(archiveRepository).findById(1L);
        verify(archiveRepository).delete(testArchive);
    }

    @Test
    @DisplayName("删除档案 - 档案不存在")
    void deleteArchive_NotFound() {
        // Given
        when(archiveRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ArchiveNotFoundException.class, () -> {
            archiveService.deleteArchive(1L);
        });

        verify(archiveRepository).findById(1L);
        verify(archiveRepository, never()).delete(any(Archive.class));
    }

    @Test
    @DisplayName("分页获取档案列表 - 成功")
    void getArchiveList_Success() {
        // Given
        List<Archive> archives = Arrays.asList(testArchive);
        Page<Archive> archivePage = new PageImpl<>(archives);
        Pageable pageable = PageRequest.of(0, 10);

        when(archiveRepository.findAll(pageable)).thenReturn(archivePage);

        // When
        Page<ArchiveDTO> result = archiveService.getArchiveList(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(testArchive.getTitle(), result.getContent().get(0).getTitle());

        verify(archiveRepository).findAll(pageable);
    }

    @Test
    @DisplayName("根据状态获取档案列表 - 成功")
    void getArchivesByStatus_Success() {
        // Given
        List<Archive> archives = Arrays.asList(testArchive);
        when(archiveRepository.findByStatus(ArchiveStatus.ACTIVE)).thenReturn(archives);

        // When
        List<ArchiveDTO> result = archiveService.getArchivesByStatus(ArchiveStatus.ACTIVE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testArchive.getTitle(), result.get(0).getTitle());
        assertEquals(ArchiveStatus.ACTIVE, result.get(0).getStatus());

        verify(archiveRepository).findByStatus(ArchiveStatus.ACTIVE);
    }

    @Test
    @DisplayName("根据类型获取档案列表 - 成功")
    void getArchivesByType_Success() {
        // Given
        List<Archive> archives = Arrays.asList(testArchive);
        when(archiveRepository.findByType(ArchiveType.DOCUMENT)).thenReturn(archives);

        // When
        List<ArchiveDTO> result = archiveService.getArchivesByType(ArchiveType.DOCUMENT);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testArchive.getTitle(), result.get(0).getTitle());
        assertEquals(ArchiveType.DOCUMENT, result.get(0).getType());

        verify(archiveRepository).findByType(ArchiveType.DOCUMENT);
    }

    @Test
    @DisplayName("搜索档案 - 成功")
    void searchArchives_Success() {
        // Given
        String keyword = "测试";
        List<Archive> archives = Arrays.asList(testArchive);
        when(archiveRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword))
                .thenReturn(archives);

        // When
        List<ArchiveDTO> result = archiveService.searchArchives(keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testArchive.getTitle(), result.get(0).getTitle());

        verify(archiveRepository).findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }

    @Test
    @DisplayName("归档档案 - 成功")
    void archiveDocument_Success() {
        // Given
        when(archiveRepository.findById(1L)).thenReturn(Optional.of(testArchive));
        when(archiveRepository.save(any(Archive.class))).thenReturn(testArchive);

        // When
        ArchiveDTO result = archiveService.archiveDocument(1L);

        // Then
        assertNotNull(result);
        verify(archiveRepository).findById(1L);
        verify(archiveRepository).save(any(Archive.class));
    }

    @Test
    @DisplayName("恢复档案 - 成功")
    void restoreArchive_Success() {
        // Given
        testArchive.setStatus(ArchiveStatus.ARCHIVED);
        when(archiveRepository.findById(1L)).thenReturn(Optional.of(testArchive));
        when(archiveRepository.save(any(Archive.class))).thenReturn(testArchive);

        // When
        ArchiveDTO result = archiveService.restoreArchive(1L);

        // Then
        assertNotNull(result);
        verify(archiveRepository).findById(1L);
        verify(archiveRepository).save(any(Archive.class));
    }

    @Test
    @DisplayName("获取用户创建的档案 - 成功")
    void getArchivesByCreator_Success() {
        // Given
        List<Archive> archives = Arrays.asList(testArchive);
        when(archiveRepository.findByCreatedBy(testUser)).thenReturn(archives);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        List<ArchiveDTO> result = archiveService.getArchivesByCreator(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testArchive.getTitle(), result.get(0).getTitle());

        verify(userRepository).findById(1L);
        verify(archiveRepository).findByCreatedBy(testUser);
    }

    @Test
    @DisplayName("统计档案数量 - 成功")
    void countArchives_Success() {
        // Given
        when(archiveRepository.count()).thenReturn(10L);

        // When
        long result = archiveService.countArchives();

        // Then
        assertEquals(10L, result);
        verify(archiveRepository).count();
    }

    @Test
    @DisplayName("根据状态统计档案数量 - 成功")
    void countArchivesByStatus_Success() {
        // Given
        when(archiveRepository.countByStatus(ArchiveStatus.ACTIVE)).thenReturn(5L);

        // When
        long result = archiveService.countArchivesByStatus(ArchiveStatus.ACTIVE);

        // Then
        assertEquals(5L, result);
        verify(archiveRepository).countByStatus(ArchiveStatus.ACTIVE);
    }
}