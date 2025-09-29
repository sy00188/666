package com.archive.management.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 树形结构工具类
 * 提供树形数据结构操作的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class TreeUtil {

    /**
     * 树节点接口
     */
    public interface TreeNode<T> {
        /**
         * 获取节点ID
         * 
         * @return 节点ID
         */
        T getId();

        /**
         * 获取父节点ID
         * 
         * @return 父节点ID
         */
        T getParentId();

        /**
         * 获取子节点列表
         * 
         * @return 子节点列表
         */
        List<? extends TreeNode<T>> getChildren();

        /**
         * 设置子节点列表
         * 
         * @param children 子节点列表
         */
        void setChildren(List<? extends TreeNode<T>> children);

        /**
         * 获取节点层级
         * 
         * @return 节点层级
         */
        default Integer getLevel() {
            return 0;
        }

        /**
         * 设置节点层级
         * 
         * @param level 节点层级
         */
        default void setLevel(Integer level) {
            // 默认实现为空
        }
    }

    /**
     * 简单树节点实现
     */
    public static class SimpleTreeNode implements TreeNode<Long> {
        private Long id;
        private Long parentId;
        private String name;
        private Integer level;
        private List<SimpleTreeNode> children;
        private Map<String, Object> extra;

        public SimpleTreeNode() {
            this.children = new ArrayList<>();
            this.extra = new HashMap<>();
        }

        public SimpleTreeNode(Long id, Long parentId, String name) {
            this();
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        @Override
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Integer getLevel() {
            return level;
        }

        @Override
        public void setLevel(Integer level) {
            this.level = level;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<SimpleTreeNode> getChildren() {
            return children;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void setChildren(List<? extends TreeNode<Long>> children) {
            this.children = (List<SimpleTreeNode>) children;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }

        public void putExtra(String key, Object value) {
            this.extra.put(key, value);
        }

        public Object getExtra(String key) {
            return this.extra.get(key);
        }
    }

    // ========== 树构建 ==========

    /**
     * 构建树形结构
     * 
     * @param nodes 节点列表
     * @param rootValue 根节点值
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 树形结构
     */
    public static <T, N extends TreeNode<T>> List<N> buildTree(List<N> nodes, T rootValue) {
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建ID到节点的映射
        Map<T, N> nodeMap = nodes.stream()
            .collect(Collectors.toMap(TreeNode::getId, Function.identity()));

        // 构建树形结构
        List<N> rootNodes = new ArrayList<>();
        
        for (N node : nodes) {
            T parentId = node.getParentId();
            
            if (Objects.equals(parentId, rootValue)) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点
                N parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<N> children = (List<N>) parentNode.getChildren();
                    if (children == null) {
                        children = new ArrayList<>();
                        parentNode.setChildren(children);
                    }
                    children.add(node);
                }
            }
        }

        return rootNodes;
    }

    /**
     * 构建树形结构（自动识别根节点）
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 树形结构
     */
    public static <T, N extends TreeNode<T>> List<N> buildTree(List<N> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有ID
        Set<T> allIds = nodes.stream()
            .map(TreeNode::getId)
            .collect(Collectors.toSet());

        // 找出根节点（parentId不在所有ID中的节点）
        List<N> rootNodes = nodes.stream()
            .filter(node -> node.getParentId() == null || !allIds.contains(node.getParentId()))
            .collect(Collectors.toList());

        // 创建ID到节点的映射
        Map<T, N> nodeMap = nodes.stream()
            .collect(Collectors.toMap(TreeNode::getId, Function.identity()));

        // 构建子节点关系
        for (N node : nodes) {
            T parentId = node.getParentId();
            if (parentId != null) {
                N parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<N> children = (List<N>) parentNode.getChildren();
                    if (children == null) {
                        children = new ArrayList<>();
                        parentNode.setChildren(children);
                    }
                    children.add(node);
                }
            }
        }

        return rootNodes;
    }

    /**
     * 构建树形结构并设置层级
     * 
     * @param nodes 节点列表
     * @param rootValue 根节点值
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 树形结构
     */
    public static <T, N extends TreeNode<T>> List<N> buildTreeWithLevel(List<N> nodes, T rootValue) {
        List<N> tree = buildTree(nodes, rootValue);
        setTreeLevel(tree, 0);
        return tree;
    }

    /**
     * 设置树节点层级
     * 
     * @param nodes 节点列表
     * @param level 当前层级
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     */
    private static <T, N extends TreeNode<T>> void setTreeLevel(List<N> nodes, int level) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (N node : nodes) {
            node.setLevel(level);
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children != null && !children.isEmpty()) {
                setTreeLevel(children, level + 1);
            }
        }
    }

    // ========== 树遍历 ==========

    /**
     * 深度优先遍历
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 遍历结果
     */
    public static <T, N extends TreeNode<T>> List<N> depthFirstTraversal(List<N> nodes) {
        List<N> result = new ArrayList<>();
        if (nodes == null || nodes.isEmpty()) {
            return result;
        }

        for (N node : nodes) {
            result.add(node);
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children != null && !children.isEmpty()) {
                result.addAll(depthFirstTraversal(children));
            }
        }

        return result;
    }

    /**
     * 广度优先遍历
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 遍历结果
     */
    public static <T, N extends TreeNode<T>> List<N> breadthFirstTraversal(List<N> nodes) {
        List<N> result = new ArrayList<>();
        if (nodes == null || nodes.isEmpty()) {
            return result;
        }

        Queue<N> queue = new LinkedList<>(nodes);
        
        while (!queue.isEmpty()) {
            N node = queue.poll();
            result.add(node);
            
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children != null && !children.isEmpty()) {
                queue.addAll(children);
            }
        }

        return result;
    }

    /**
     * 按层级遍历
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 按层级分组的遍历结果
     */
    public static <T, N extends TreeNode<T>> Map<Integer, List<N>> levelTraversal(List<N> nodes) {
        Map<Integer, List<N>> result = new HashMap<>();
        if (nodes == null || nodes.isEmpty()) {
            return result;
        }

        Queue<N> queue = new LinkedList<>(nodes);
        
        // 设置根节点层级
        for (N node : nodes) {
            node.setLevel(0);
        }
        
        while (!queue.isEmpty()) {
            N node = queue.poll();
            int level = node.getLevel();
            
            result.computeIfAbsent(level, k -> new ArrayList<>()).add(node);
            
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children != null && !children.isEmpty()) {
                for (N child : children) {
                    child.setLevel(level + 1);
                    queue.add(child);
                }
            }
        }

        return result;
    }

    // ========== 树查找 ==========

    /**
     * 根据ID查找节点
     * 
     * @param nodes 节点列表
     * @param id 节点ID
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 找到的节点
     */
    public static <T, N extends TreeNode<T>> N findNodeById(List<N> nodes, T id) {
        if (nodes == null || nodes.isEmpty() || id == null) {
            return null;
        }

        for (N node : nodes) {
            if (Objects.equals(node.getId(), id)) {
                return node;
            }
            
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children != null && !children.isEmpty()) {
                N found = findNodeById(children, id);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * 根据条件查找节点
     * 
     * @param nodes 节点列表
     * @param predicate 查找条件
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 找到的节点列表
     */
    public static <T, N extends TreeNode<T>> List<N> findNodes(List<N> nodes, Predicate<N> predicate) {
        List<N> result = new ArrayList<>();
        if (nodes == null || nodes.isEmpty() || predicate == null) {
            return result;
        }

        for (N node : nodes) {
            if (predicate.test(node)) {
                result.add(node);
            }
            
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children != null && !children.isEmpty()) {
                result.addAll(findNodes(children, predicate));
            }
        }

        return result;
    }

    /**
     * 查找节点路径
     * 
     * @param nodes 节点列表
     * @param id 目标节点ID
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 节点路径
     */
    public static <T, N extends TreeNode<T>> List<N> findPath(List<N> nodes, T id) {
        if (nodes == null || nodes.isEmpty() || id == null) {
            return new ArrayList<>();
        }

        for (N node : nodes) {
            List<N> path = new ArrayList<>();
            if (findPathRecursive(node, id, path)) {
                return path;
            }
        }

        return new ArrayList<>();
    }

    /**
     * 递归查找路径
     * 
     * @param node 当前节点
     * @param id 目标节点ID
     * @param path 路径列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 是否找到
     */
    private static <T, N extends TreeNode<T>> boolean findPathRecursive(N node, T id, List<N> path) {
        path.add(node);
        
        if (Objects.equals(node.getId(), id)) {
            return true;
        }
        
        @SuppressWarnings("unchecked")
        List<N> children = (List<N>) node.getChildren();
        if (children != null && !children.isEmpty()) {
            for (N child : children) {
                if (findPathRecursive(child, id, path)) {
                    return true;
                }
            }
        }
        
        path.remove(path.size() - 1);
        return false;
    }

    // ========== 树操作 ==========

    /**
     * 获取所有叶子节点
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 叶子节点列表
     */
    public static <T, N extends TreeNode<T>> List<N> getLeafNodes(List<N> nodes) {
        List<N> leafNodes = new ArrayList<>();
        if (nodes == null || nodes.isEmpty()) {
            return leafNodes;
        }

        for (N node : nodes) {
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children == null || children.isEmpty()) {
                leafNodes.add(node);
            } else {
                leafNodes.addAll(getLeafNodes(children));
            }
        }

        return leafNodes;
    }

    /**
     * 获取树的最大深度
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 最大深度
     */
    public static <T, N extends TreeNode<T>> int getMaxDepth(List<N> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }

        int maxDepth = 0;
        for (N node : nodes) {
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            int depth = 1 + getMaxDepth(children);
            maxDepth = Math.max(maxDepth, depth);
        }

        return maxDepth;
    }

    /**
     * 获取节点数量
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 节点数量
     */
    public static <T, N extends TreeNode<T>> int getNodeCount(List<N> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }

        int count = nodes.size();
        for (N node : nodes) {
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            count += getNodeCount(children);
        }

        return count;
    }

    /**
     * 过滤树节点
     * 
     * @param nodes 节点列表
     * @param predicate 过滤条件
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 过滤后的树
     */
    public static <T, N extends TreeNode<T>> List<N> filterTree(List<N> nodes, Predicate<N> predicate) {
        if (nodes == null || nodes.isEmpty() || predicate == null) {
            return new ArrayList<>();
        }

        List<N> result = new ArrayList<>();
        
        for (N node : nodes) {
            if (predicate.test(node)) {
                result.add(node);
                
                @SuppressWarnings("unchecked")
                List<N> children = (List<N>) node.getChildren();
                if (children != null && !children.isEmpty()) {
                    List<N> filteredChildren = filterTree(children, predicate);
                    node.setChildren(filteredChildren);
                }
            }
        }

        return result;
    }

    /**
     * 排序树节点
     * 
     * @param nodes 节点列表
     * @param comparator 比较器
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     */
    public static <T, N extends TreeNode<T>> void sortTree(List<N> nodes, Comparator<N> comparator) {
        if (nodes == null || nodes.isEmpty() || comparator == null) {
            return;
        }

        nodes.sort(comparator);
        
        for (N node : nodes) {
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children != null && !children.isEmpty()) {
                sortTree(children, comparator);
            }
        }
    }

    // ========== 树转换 ==========

    /**
     * 树转扁平列表
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 扁平列表
     */
    public static <T, N extends TreeNode<T>> List<N> treeToList(List<N> nodes) {
        return depthFirstTraversal(nodes);
    }

    /**
     * 扁平列表转树
     * 
     * @param nodes 扁平节点列表
     * @param rootValue 根节点值
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 树形结构
     */
    public static <T, N extends TreeNode<T>> List<N> listToTree(List<N> nodes, T rootValue) {
        return buildTree(nodes, rootValue);
    }

    /**
     * 树转Map（ID为键）
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return Map结构
     */
    public static <T, N extends TreeNode<T>> Map<T, N> treeToMap(List<N> nodes) {
        List<N> flatList = treeToList(nodes);
        return flatList.stream()
            .collect(Collectors.toMap(TreeNode::getId, Function.identity()));
    }

    // ========== 树验证 ==========

    /**
     * 验证树结构是否有效
     * 
     * @param nodes 节点列表
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 验证结果
     */
    public static <T, N extends TreeNode<T>> boolean isValidTree(List<N> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return true;
        }

        // 检查是否有循环引用
        Set<T> visited = new HashSet<>();
        for (N node : nodes) {
            if (hasCycle(node, visited, new HashSet<>())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查是否有循环引用
     * 
     * @param node 当前节点
     * @param visited 已访问节点
     * @param path 当前路径
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 是否有循环
     */
    private static <T, N extends TreeNode<T>> boolean hasCycle(N node, Set<T> visited, Set<T> path) {
        T nodeId = node.getId();
        
        if (path.contains(nodeId)) {
            return true; // 发现循环
        }
        
        if (visited.contains(nodeId)) {
            return false; // 已经访问过，无循环
        }
        
        visited.add(nodeId);
        path.add(nodeId);
        
        @SuppressWarnings("unchecked")
        List<N> children = (List<N>) node.getChildren();
        if (children != null) {
            for (N child : children) {
                if (hasCycle(child, visited, path)) {
                    return true;
                }
            }
        }
        
        path.remove(nodeId);
        return false;
    }

    // ========== 工具方法 ==========

    /**
     * 创建简单树节点
     * 
     * @param id 节点ID
     * @param parentId 父节点ID
     * @param name 节点名称
     * @return 简单树节点
     */
    public static SimpleTreeNode createSimpleNode(Long id, Long parentId, String name) {
        return new SimpleTreeNode(id, parentId, name);
    }

    /**
     * 打印树结构
     * 
     * @param nodes 节点列表
     * @param nameExtractor 名称提取器
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     * @return 树结构字符串
     */
    public static <T, N extends TreeNode<T>> String printTree(List<N> nodes, Function<N, String> nameExtractor) {
        StringBuilder sb = new StringBuilder();
        printTreeRecursive(nodes, nameExtractor, "", sb);
        return sb.toString();
    }

    /**
     * 递归打印树结构
     * 
     * @param nodes 节点列表
     * @param nameExtractor 名称提取器
     * @param prefix 前缀
     * @param sb 字符串构建器
     * @param <T> 节点ID类型
     * @param <N> 节点类型
     */
    private static <T, N extends TreeNode<T>> void printTreeRecursive(List<N> nodes, 
                                                                      Function<N, String> nameExtractor, 
                                                                      String prefix, 
                                                                      StringBuilder sb) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            N node = nodes.get(i);
            boolean isLast = i == nodes.size() - 1;
            
            sb.append(prefix)
              .append(isLast ? "└── " : "├── ")
              .append(nameExtractor.apply(node))
              .append("\n");
            
            @SuppressWarnings("unchecked")
            List<N> children = (List<N>) node.getChildren();
            if (children != null && !children.isEmpty()) {
                String childPrefix = prefix + (isLast ? "    " : "│   ");
                printTreeRecursive(children, nameExtractor, childPrefix, sb);
            }
        }
    }
}