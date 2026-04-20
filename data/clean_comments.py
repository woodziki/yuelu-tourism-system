import csv
import hashlib
import re
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
INPUT_FILE = BASE_DIR / "comments.csv"
OUTPUT_FILE = BASE_DIR / "import_comments_with_users.sql"

SPOT_NAME_MAP = {
    "中南大学(岳麓山校区)": "中南大学",
    "古麓山寺": "麓山寺",
    "长沙之眼": "长沙电视塔(长沙之眼)",
}

DEFAULT_PASSWORD = "$2a$10$nGAqi.DyHXNCO6f3K6auLu3oiEoV2Gra3b.TUsJVE1OJYRRpvLYjG"
DEFAULT_TIME = "2026-05-04 00:00:00"


def sql_escape(value):
    return value.replace("\\", "\\\\").replace("'", "''")


def clean_text(value):
    return re.sub(r"\s+", " ", value or "").strip()


def parse_date(value):
    match = re.search(r"\d{4}-\d{2}-\d{2}", value or "")
    if not match:
        return DEFAULT_TIME
    return f"{match.group(0)} 00:00:00"


def build_username(nickname):
    digest = hashlib.md5(nickname.encode("utf-8")).hexdigest()[:12]
    return f"crawler_{digest}"


def read_comments():
    users = {}
    comments = []
    seen_comments = set()
    skipped = 0

    with INPUT_FILE.open("r", encoding="utf-8-sig", newline="") as file:
        reader = csv.reader(file)
        for row in reader:
            if len(row) < 6:
                skipped += 1
                continue

            spot_name = SPOT_NAME_MAP.get(clean_text(row[0]), clean_text(row[0]))
            star_text = clean_text(row[1])
            content = clean_text(row[2])
            create_time = parse_date(row[3])
            nickname = clean_text(row[4])

            if not spot_name or not content or not nickname or not star_text.isdigit():
                skipped += 1
                continue

            star = int(star_text)
            if star < 1 or star > 5:
                skipped += 1
                continue

            username = build_username(nickname)
            users[username] = nickname

            comment_key = (username, spot_name, content, create_time)
            if comment_key in seen_comments:
                continue

            seen_comments.add(comment_key)
            comments.append((username, spot_name, content, star, create_time))

    return users, comments, skipped


def write_sql(users, comments, skipped):
    with OUTPUT_FILE.open("w", encoding="utf-8", newline="\n") as file:
        file.write("USE yuelu_tourism;\n")
        file.write("SET NAMES utf8mb4;\n\n")
        file.write("-- 导入爬虫评论用户\n")

        for username, nickname in sorted(users.items()):
            file.write(
                "INSERT IGNORE INTO t_user "
                "(username, password, nickname, status, create_time) VALUES "
                f"('{sql_escape(username)}', '{DEFAULT_PASSWORD}', '{sql_escape(nickname)}', 0, NOW());\n"
            )

        file.write("\n-- 导入爬虫评论，景点名称不存在时自动跳过\n")
        for username, spot_name, content, star, create_time in comments:
            file.write(
                "INSERT INTO t_comment (user_id, spot_id, content, star, create_time) "
                "SELECT u.id, s.id, "
                f"'{sql_escape(content)}', {star}, '{create_time}' "
                "FROM t_user u JOIN t_spot s "
                f"WHERE u.username = '{sql_escape(username)}' "
                f"AND s.name = '{sql_escape(spot_name)}' "
                "AND NOT EXISTS ("
                "SELECT 1 FROM t_comment c "
                "WHERE c.user_id = u.id "
                "AND c.spot_id = s.id "
                f"AND c.content = '{sql_escape(content)}' "
                f"AND c.create_time = '{create_time}'"
                ");\n"
            )

        file.write("\n")
        file.write(f"-- 用户数：{len(users)}\n")
        file.write(f"-- 评论数：{len(comments)}\n")
        file.write(f"-- 跳过无效行数：{skipped}\n")


def main():
    users, comments, skipped = read_comments()
    write_sql(users, comments, skipped)
    print(f"已生成：{OUTPUT_FILE}")
    print(f"用户数：{len(users)}")
    print(f"评论数：{len(comments)}")
    print(f"跳过无效行数：{skipped}")


if __name__ == "__main__":
    main()
