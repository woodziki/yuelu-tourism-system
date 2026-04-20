"""
携程景点评论爬虫
策略：Selenium 访问页面获取首批 SSR 评论
     → 注入网络拦截器 → 模拟翻页点击 → 从拦截到的 API 响应中提取评论
"""

import csv
import json
import os
import random
import re
import time
import traceback

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.edge.options import Options as EdgeOptions

# ==================== 配置 ====================

SPOTS = {
    "岳麓书院": "https://you.ctrip.com/sight/changsha148/9013.html",
    "东方红广场":"https://you.ctrip.com/sight/changsha148/1714337.html",
    "爱晚亭":"https://you.ctrip.com/sight/changsha148/8982.html",
    "长沙之眼":"https://you.ctrip.com/sight/changsha148/2030258.html",
    "岳麓山索道":"https://you.ctrip.com/sight/changsha148/69415109.html",
    "古麓山寺":"https://you.ctrip.com/sight/changsha148/9014.html",
    "云麓宫":"https://you.ctrip.com/sight/changsha148/9018.html",
    "穿石坡湖":"https://you.ctrip.com/sight/changsha148/1673187.html",
    "白鹤泉":"https://you.ctrip.com/sight/changsha148/9020.html",
    "黄兴墓":"https://you.ctrip.com/sight/changsha148/9016.html",
    "蔡锷墓":"https://you.ctrip.com/sight/changsha148/9017.html",
    "舍利塔":"https://you.ctrip.com/sight/changsha148/9009.html",
    "岳王亭":"https://you.ctrip.com/sight/changsha148/2030259.html",
    "万景园":"https://you.ctrip.com/sight/changsha148/1673018.html",
    "鸟语林":"https://you.ctrip.com/sight/changsha148/144290627.html",
    "观光长廊":"https://you.ctrip.com/sight/changsha148/1673211.html?",
    "中南大学(岳麓山校区)":"https://you.ctrip.com/sight/changsha148/1835571.html",
    "长沙会战碑":"https://you.ctrip.com/sight/changsha148/1673221.html",




    # 在这里添加更多景点，格式：  "景点名称": "携程景点页面URL",
}

MAX_PAGES = 500
PAGE_SIZE = 10

CSV_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "comments.csv")
CSV_HEADER = ["景点名称", "评分", "评论内容", "发布时间", "用户名", "点赞数"]


# ==================== 工具函数 ====================

def extract_poi_id(url):
    m = re.search(r"/(\d+)\.html", url)
    return int(m.group(1)) if m else None


def extract_next_data(driver):
    raw = driver.execute_script("""
        var el = document.getElementById('__NEXT_DATA__');
        return el ? el.textContent : null;
    """)
    return json.loads(raw) if raw else None


def parse_comment(comment, spot_name):
    content = comment.get("content", "")
    if not content:
        return None
    score = comment.get("score", "")
    pub_time = comment.get("publishTypeTag", "")
    user_info = comment.get("userInfo", {})
    user = user_info.get("userNick", "匿名用户") if isinstance(user_info, dict) else "匿名用户"
    likes = comment.get("usefulCount", 0) or comment.get("likeCount", 0) or 0
    return [spot_name, score, content, pub_time, user, likes]


# ==================== 浏览器 ====================

def create_driver():
    options = EdgeOptions()
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--lang=zh-CN")
    options.add_argument("--window-size=1366,768")
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    options.add_experimental_option("useAutomationExtension", False)

    driver = webdriver.Edge(options=options)
    driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
        "source": "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
    })
    driver.set_page_load_timeout(30)
    return driver


# ==================== 网络拦截 ====================

INTERCEPTOR_JS = """
window.__captured_comments = [];

// 拦截 fetch
if (!window.__fetch_hooked) {
    const origFetch = window.fetch;
    window.fetch = async function(...args) {
        const response = await origFetch.apply(this, args);
        const url = (typeof args[0] === 'string') ? args[0] : (args[0] && args[0].url) || '';
        if (url.includes('getPoiComment') || url.includes('Comment') || url.includes('comment')) {
            try {
                const clone = response.clone();
                const data = await clone.json();
                if (data && data.result) {
                    window.__captured_comments.push(data);
                }
            } catch(e) {}
        }
        return response;
    };
    window.__fetch_hooked = true;
}

// 拦截 XMLHttpRequest
if (!window.__xhr_hooked) {
    const origOpen = XMLHttpRequest.prototype.open;
    const origSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.open = function(method, url) {
        this.__url = url;
        return origOpen.apply(this, arguments);
    };
    XMLHttpRequest.prototype.send = function() {
        this.addEventListener('load', function() {
            var u = this.__url || '';
            if (u.includes('getPoiComment') || u.includes('Comment') || u.includes('comment')) {
                try {
                    var data = JSON.parse(this.responseText);
                    if (data && data.result) {
                        window.__captured_comments.push(data);
                    }
                } catch(e) {}
            }
        });
        return origSend.apply(this, arguments);
    };
    window.__xhr_hooked = true;
}
"""


def inject_response_interceptor(driver):
    driver.execute_script(INTERCEPTOR_JS)


def get_intercepted_comments(driver):
    data = driver.execute_script("""
        var result = window.__captured_comments || [];
        window.__captured_comments = [];
        return result;
    """)
    return data


# ==================== 翻页操作 ====================

def scroll_to_comments(driver):
    driver.execute_script("""
        var el = document.querySelector(
            '[id*="commentModule"], [id*="comment"], [class*="commentModule"], '
          + '[class*="CommentModule"], [data-anchor*="comment"]'
        );
        if (el) {
            el.scrollIntoView({block: 'center'});
        } else {
            window.scrollTo(0, document.body.scrollHeight * 0.5);
        }
    """)
    time.sleep(1.5)


def click_next_page(driver):
    """点击"下一页"按钮。成功返回 True。"""

    # 策略 1：CSS 选择器
    css_selectors = [
        ".ant-pagination-next:not(.ant-pagination-disabled) button",
        ".ant-pagination-next:not(.ant-pagination-disabled)",
        "li[class*='next']:not([class*='disabled']) a",
        "a[class*='next']:not([class*='disabled'])",
        "button[class*='next']:not(:disabled)",
        "[class*='pagination'] [class*='next']:not([class*='disabled'])",
        "[aria-label='Next']",
        "[aria-label='next']",
    ]
    for sel in css_selectors:
        try:
            el = driver.find_element(By.CSS_SELECTOR, sel)
            if el.is_displayed() and el.is_enabled():
                driver.execute_script("arguments[0].scrollIntoView({block:'center'});", el)
                time.sleep(0.5)
                try:
                    el.click()
                except Exception:
                    driver.execute_script("arguments[0].click();", el)
                return True
        except Exception:
            continue

    # 策略 2：根据当前页码点击下一个数字
    try:
        active = driver.find_element(By.CSS_SELECTOR,
            ".ant-pagination-item-active, [class*='active'][class*='page']")
        cur = int(active.text.strip())
        targets = driver.find_elements(By.CSS_SELECTOR,
            ".ant-pagination-item, [class*='page-item'], [class*='paginationItem']")
        for t in targets:
            if t.text.strip() == str(cur + 1) and t.is_displayed():
                driver.execute_script("arguments[0].scrollIntoView({block:'center'});", t)
                time.sleep(0.3)
                t.click()
                return True
    except Exception:
        pass

    # 策略 3：按文本查找
    try:
        for tag in ("a", "button", "li", "span"):
            for el in driver.find_elements(By.TAG_NAME, tag):
                txt = el.text.strip()
                if txt in ("下一页", "›", ">", "»", "Next") and el.is_displayed():
                    driver.execute_script("arguments[0].scrollIntoView({block:'center'});", el)
                    time.sleep(0.3)
                    try:
                        el.click()
                    except Exception:
                        driver.execute_script("arguments[0].click();", el)
                    return True
    except Exception:
        pass

    return False


# ==================== 主流程 ====================

def crawl():
    print("=" * 50)
    print("携程景点评论爬虫 (Selenium 翻页模式)")
    print("=" * 50)

    driver = create_driver()

    try:
        write_header = not os.path.exists(CSV_PATH) or os.path.getsize(CSV_PATH) == 0
        f = open(CSV_PATH, mode="a", newline="", encoding="utf-8-sig")
        writer = csv.writer(f)
        if write_header:
            writer.writerow(CSV_HEADER)

        try:
            for spot_name, url in SPOTS.items():
                poi_id = extract_poi_id(url)
                if not poi_id:
                    print(f"\n无法提取 poiId: {url}")
                    continue

                print(f"\n{'─' * 40}")
                print(f"景点: {spot_name}")

                # 1) 访问页面，提取 SSR 数据
                print("  [1] 访问页面...")
                driver.get(url)
                time.sleep(random.uniform(4, 6))
                print(f"      标题: {driver.title}")

                next_data = extract_next_data(driver)
                if not next_data:
                    print("      未找到 __NEXT_DATA__，跳过")
                    continue

                state = next_data.get("props", {}).get("pageProps", {}).get("initialState", {})
                comment_info = state.get("poiCommentInfo", {})
                total_count = (state.get("commentTotalCount", 0)
                               or comment_info.get("commentCount", 0))
                print(f"      总评论数: {total_count}")

                # 2) 提取首批 SSR 评论
                first_batch = state.get("commentList", [])
                rows = []
                for c in first_batch:
                    row = parse_comment(c, spot_name)
                    if row:
                        rows.append(row)
                print(f"  [2] SSR 提取 {len(rows)} 条首屏评论")

                if total_count <= len(rows):
                    print("      首屏已包含全部评论，跳过翻页")
                else:
                    # 3) 注入网络拦截器 & 滚动到评论区
                    inject_response_interceptor(driver)
                    scroll_to_comments(driver)
                    time.sleep(1)

                    # 4) 翻页抓取
                    print("  [3] Selenium 翻页抓取...")
                    max_page = min(MAX_PAGES, (total_count // PAGE_SIZE) + 1)
                    empty_count = 0

                    for page in range(2, max_page + 1):
                        print(f"      第 {page}/{max_page} 页...", end=" ", flush=True)

                        if not click_next_page(driver):
                            print("未找到翻页按钮，停止")
                            break

                        time.sleep(random.uniform(2, 4))

                        # 从拦截器获取评论数据
                        captured = get_intercepted_comments(driver)
                        page_rows = []
                        for resp in captured:
                            result = resp.get("result", {})
                            if not isinstance(result, dict):
                                continue
                            items = (result.get("commentList")
                                     or result.get("items")
                                     or [])
                            for c in items:
                                row = parse_comment(c, spot_name)
                                if row:
                                    page_rows.append(row)

                        if page_rows:
                            rows.extend(page_rows)
                            print(f"获取 {len(page_rows)} 条")
                            empty_count = 0
                        else:
                            print("无数据 (拦截器未捕获到评论)")
                            empty_count += 1
                            if empty_count >= 3:
                                print("      连续无数据，停止翻页")
                                break

                        time.sleep(random.uniform(1, 2))

                # 5) 写入 CSV
                if rows:
                    writer.writerows(rows)
                    f.flush()
                    print(f"\n  => {spot_name} 共写入 {len(rows)} 条评论")
                else:
                    print(f"\n  => {spot_name} 未获取到评论")

                if spot_name != list(SPOTS.keys())[-1]:
                    time.sleep(random.uniform(5, 8))

        finally:
            f.close()

        print(f"\n{'=' * 50}")
        print(f"完成！结果保存在: {CSV_PATH}")

    except KeyboardInterrupt:
        print("\n用户中断，已保存已抓取的数据。")
    except Exception as e:
        print(f"\n错误: {e}")
        traceback.print_exc()
    finally:
        try:
            driver.quit()
        except Exception:
            pass


if __name__ == "__main__":
    crawl()
