#!/usr/bin/env python3
"""
交互式审查门控脚本 (final_review_gate.py)
用于RIPER-5协议中EXECUTE模式的条件性交互式步骤审查
"""

import sys
import signal

def signal_handler(sig, frame):
    """处理中断信号"""
    print("\n[GATE] 审查门控被用户中断")
    sys.exit(0)

def main():
    """主函数：处理用户输入并格式化输出"""
    # 注册信号处理器
    signal.signal(signal.SIGINT, signal_handler)
    
    print("[GATE] 交互式审查门控已激活")
    print("[GATE] 请输入您的子提示来进行迭代修改，或输入结束关键字来完成审查")
    print("[GATE] 结束关键字：TASK_COMPLETE, 完成, 下一步, DONE, FINISH, 继续")
    print("[GATE] 输入 'help' 查看帮助信息")
    print("-" * 50)
    
    # 结束关键字列表
    end_keywords = {
        'TASK_COMPLETE', '完成', '下一步', 'DONE', 'FINISH', '继续',
        'task_complete', 'done', 'finish'
    }
    
    try:
        while True:
            # 获取用户输入
            user_input = input("[REVIEW] > ").strip()
            
            # 检查是否为空输入
            if not user_input:
                continue
                
            # 检查帮助命令
            if user_input.lower() == 'help':
                print("[GATE] 帮助信息：")
                print("  - 输入任何文本作为子提示，AI将根据您的指令进行迭代修改")
                print("  - 输入结束关键字来完成当前步骤的审查")
                print(f"  - 结束关键字：{', '.join(sorted(end_keywords))}")
                continue
            
            # 检查是否为结束关键字
            if user_input in end_keywords:
                print(f"[GATE] 检测到结束关键字：{user_input}")
                print("[GATE] 交互式审查结束")
                break
            
            # 输出格式化的用户子提示
            print(f"USER_REVIEW_SUB_PROMPT: {user_input}")
            
    except EOFError:
        print("\n[GATE] 检测到EOF，审查门控结束")
    except KeyboardInterrupt:
        print("\n[GATE] 审查门控被用户中断")
    except Exception as e:
        print(f"[GATE] 发生错误：{e}")
    
    print("[GATE] 审查门控脚本退出")

if __name__ == "__main__":
    main()