// 菜单项接口
export interface MenuItem {
  path: string;
  title: string;
  icon: string;
  children?: MenuItem[];
  disabled?: boolean;
}

// 菜单配置
export interface MenuConfig {
  items: MenuItem[];
  collapsed: boolean;
}
