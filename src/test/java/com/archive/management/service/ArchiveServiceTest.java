package com.archive.management.service;

import com.archive.management.dto.ArchiveDTO;
import com.archive.management.entity.Archive;
import com.archive.management.entity.User;
import com.archive.management.enums.ArchiveStatus;
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
 * 测试档案管理业务逻辑的各种场景
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
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRealName("测试用户");

        // 初始化测试档案
        testArchive = new Archive();
        testArchive.setId(1L);
        testArchive.setTitle("测试档案");
        testArchive.setCode("TEST-001");
        testArchive.setDescription("这是一个测试档案");
        testArchive.setStatus(ArchiveStatus.ACTIVE);
        testArchive.setCreateTime(LocalDateTime.now());
        testArchive.setUpdateTime(LocalDateTime.now());
        testArchive.setCreateUser(testUser);

        // 初始化测试档案DTO
        testArchiveDTO = new ArchiveDTO();
        testArchiveDTO.setId(1L);
        testArchiveDTO.setTitle("测试档案");
        testArchiveDTO.setCode("TEST-001");
        testArchiveDTO.setDescription("这是一个测试档案");
        testArchiveDTO.setStatus(ArchiveStatus.ACTIVE);
    }

    @Test
    @DisplayName("创建档案 - 成功")
    void createArchive_Success() {
        // Given
        ArchiveDTO createDTO = new ArchiveDTO();
        createDTO.setTitle("新档案");
        createDTO.setCode("NEW-001");
        createDTO.setDescription("新创建的档案");
        createDTO.setStatus(ArchiveStatus.ACTIVE);

        when(archiveRepository.existsByCode("NEW-001")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(archiveRepository.save(any(Archive.class))).thenReturn(testArchive);

        // When
        ArchiveDTO result = archiveService.createArchive(createDTO, 1L);

        // Then
        assertNotNull(result);
        verify(archiveRepository).existsByCode("NEW-001");
        verify(userRepository).findById(1L);
        verify(archiveRepository).save(any(Archive.class));
    }

    @Test
    @DisplayName("创建档案 - 档案编号已存在")
    void createArchive_CodeExists() {
        // Given
        ArchiveDTO createDTO = new ArchiveDTO();
        createDTO.setTitle("新档案");
        createDTO.setCode("EXISTING-001");
        createDTO.setDescription("新创建的档案");

        when(archiveRepository.existsByCode("EXISTING-001")).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            archiveService.createArchive(createDTO, 1L);
        });

        verify(archiveRepository).existsByCode("EXISTING-001");
        verify(archiveRepository, never()).save(any(Archive.class));
    }

    @Test
    @DisplayName("创建档案 - 用户不存在")
    void createArchive_UserNotFound() {
        // Given
        ArchiveDTO createDTO = new ArchiveDTO();
        createDTO.setTitle("新档案");
        createDTO.setCode("NEW-001");

        when(archiveRepository.existsByCode("NEW-001")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> {
            archiveService.createArchive(createDTO, 1L);
        });

        verify(archiveRepository).existsByCode("NEW-001");
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
        assertEquals(testArchive.getCode(), result.getCode());
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
    @DisplayName("根据编号获取档案 - 成功")
    void getArchiveByCode_Success() {
        // Given
        when(archiveRepository.findByCode("TEST-001")).thenReturn(Optional.of(testArchive));

        // When
        ArchiveDTO result = archiveService.getArchiveByCode("TEST-001");

        // Then
        assertNotNull(result);
        assertEquals(testArchive.getCode(), result.getCode());
        assertEquals(testArchive.getTitle(), result.getTitle());

        verify(archiveRepository).findByCode("TEST-001");
    }

    @Test
    @DisplayName("根据编号获取档案 - 档案不存在")
    void getArchiveByCode_NotFound() {
        // Given
        when(archiveRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ArchiveNotFoundException.class, () -> {
            archiveService.getArchiveByCode("NONEXISTENT");
        });

        verify(archiveRepository).findByCode("NONEXISTENT");
    }

    @Test
    @DisplayName("更新档案 - 成功")
    void updateArchive_Success() {
        // Given
        ArchiveDTO updateDTO = new ArchiveDTO();
        updateDTO.setTitle("更新后的档案");
        updateDTO.setDescription("更新后的描述");

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
        updateDTO.setTitle("更新后的档案");

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
    @DisplayName("搜索档案 - 成功")
    void searchArchives_Success() {
        // Given
        String keyword = "测试";
        List<Archive> archives = Arrays.asList(testArchive);
        when(archiveRepository.findByTitleContainingOrDescriptionContaining(
                keyword, keyword)).thenReturn(archives);

        // When
        List<ArchiveDTO> result = archiveService.searchArchives(keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testArchive.getTitle(), result.get(0).getTitle());

        verify(archiveRepository).findByTitleContainingOrDescriptionContaining(
                keyword, keyword);
    }

    @Test
    @DisplayName("激活档案 - 成功")
    void activateArchive_Success() {
        // Given
        testArchive.setStatus(ArchiveStatus.INACTIVE);
        when(archiveRepository.findById(1L)).thenReturn(Optional.of(testArchive));
        when(archiveRepository.save(any(Archive.class))).thenReturn(testArchive);

        // When
        ArchiveDTO result = archiveService.activateArchive(1L);

        // Then
        assertNotNull(result);
        verify(archiveRepository).findById(1L);
        verify(archiveRepository).save(any(Archive.class));
    }

    @Test
    @DisplayName("停用档案 - 成功")
    void deactivateArchive_Success() {
        // Given
        when(archiveRepository.findById(1L)).thenReturn(Optional.of(testArchive));
        when(archiveRepository.save(any(Archive.class))).thenReturn(testArchive);

        // When
        ArchiveDTO result = archiveService.deactivateArchive(1L);

        // Then
        assertNotNull(result);
        verify(archiveRepository).findById(1L);
        verify(archiveRepository).save(any(Archive.class));
    }

    @Test
    @DisplayName("根据创建用户获取档案列表 - 成功")
    void getArchivesByCreateUser_Success() {
        // Given
        List<Archive> archives = Arrays.asList(testArchive);
        when(archiveRepository.findByCreateUser(testUser)).thenReturn(archives);

        // When
        List<ArchiveDTO> result = archiveService.getArchivesByCreateUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testArchive.getTitle(), result.get(0).getTitle());

        verify(archiveRepository).findByCreateUser(testUser);
    }

    @Test
    @DisplayName("统计档案数量 - 成功")
    void countArchives_Success() {
        // Given
        when(archiveRepository.count()).thenReturn(50L);

        // When
        long result = archiveService.countArchives();

        // Then
        assertEquals(50L, result);
        verify(archiveRepository).count();
    }

    @Test
    @DisplayName("根据状态统计档案数量 - 成功")
    void countArchivesByStatus_Success() {
        // Given
        when(archiveRepository.countByStatus(ArchiveStatus.ACTIVE)).thenReturn(40L);

        // When
        long result = archiveService.countArchivesByStatus(ArchiveStatus.ACTIVE);

        // Then
        assertEquals(40L, result);
        verify(archiveRepository).countByStatus(ArchiveStatus.ACTIVE);
    }

    @Test
    @DisplayName("批量更新档案状态 - 成功")
    void batchUpdateArchiveStatus_Success() {
        // Given
        List<Long> archiveIds = Arrays.asList(1L, 2L, 3L);
        ArchiveStatus newStatus = ArchiveStatus.INACTIVE;

        when(archiveRepository.findAllById(archiveIds)).thenReturn(Arrays.asList(testArchive));
        when(archiveRepository.saveAll(anyList())).thenReturn(Arrays.asList(testArchive));

        // When
        assertDoesNotThrow(() -> {
            archiveService.batchUpdateArchiveStatus(archiveIds, newStatus);
        });

        // Then
        verify(archiveRepository).findAllById(archiveIds);
        verify(archiveRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("批量删除档案 - 成功")
    void batchDeleteArchives_Success() {
        // Given
        List<Long> archiveIds = Arrays.asList(1L, 2L, 3L);

        when(archiveRepository.findAllById(archiveIds)).thenReturn(Arrays.asList(testArchive));
        doNothing().when(archiveRepository).deleteAll(anyList());

        // When
        assertDoesNotThrow(() -> {
            archiveService.batchDeleteArchives(archiveIds);
        });

        // Then
        verify(archiveRepository).findAllById(archiveIds);
        verify(archiveRepository).deleteAll(anyList());
    }
}