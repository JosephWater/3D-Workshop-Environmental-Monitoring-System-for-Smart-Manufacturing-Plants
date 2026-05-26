from __future__ import annotations

import csv
from collections import Counter
from datetime import datetime, timedelta
from pathlib import Path

from openpyxl import Workbook


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "sample-data"
CSV_PATH = OUTPUT_DIR / "sensor_data_large.csv"
XLSX_PATH = OUTPUT_DIR / "sensor_data_large.xlsx"

HEADERS = ["sensor_code", "temperature", "humidity", "collect_time"]
SENSORS = [f"S{i}" for i in range(1, 10)]
START_TIME = datetime(2026, 5, 1, 8, 0, 0)
POINT_COUNT = 120
INTERVAL_MINUTES = 5


def build_rows() -> tuple[list[list[str]], Counter]:
    rows: list[list[str]] = []
    alarms = Counter()

    for index in range(POINT_COUNT):
        collect_time = START_TIME + timedelta(minutes=index * INTERVAL_MINUTES)
        for sensor_offset, sensor_code in enumerate(SENSORS, start=1):
            temperature = 23.5 + ((index + sensor_offset) % 7) * 0.8 + (sensor_offset % 3) * 0.3
            humidity = 48.0 + ((index * 2 + sensor_offset) % 9) * 1.9

            if index % 18 == 0 and sensor_code in {"S2", "S5"}:
                temperature = 31.6 + sensor_offset * 0.2
                alarms["TEMP_HIGH"] += 1
            elif index % 25 == 0 and sensor_code == "S7":
                temperature = 16.4
                alarms["TEMP_LOW"] += 1

            if index % 20 == 0 and sensor_code in {"S3", "S8"}:
                humidity = 74.5 + sensor_offset * 0.3
                alarms["HUM_HIGH"] += 1
            elif index % 22 == 0 and sensor_code == "S4":
                humidity = 36.8
                alarms["HUM_LOW"] += 1

            rows.append(
                [
                    sensor_code,
                    f"{temperature:.2f}",
                    f"{humidity:.2f}",
                    collect_time.isoformat(timespec="seconds"),
                ]
            )

    return rows, alarms


def write_csv(rows: list[list[str]]) -> None:
    with CSV_PATH.open("w", newline="", encoding="utf-8-sig") as file:
        writer = csv.writer(file)
        writer.writerow(HEADERS)
        writer.writerows(rows)


def write_excel(rows: list[list[str]]) -> None:
    workbook = Workbook()
    sheet = workbook.active
    sheet.title = "sensor_data"
    sheet.append(HEADERS)

    for row in rows:
        sheet.append([row[0], float(row[1]), float(row[2]), row[3]])

    for column_cells in sheet.columns:
        max_length = max(len(str(cell.value)) if cell.value is not None else 0 for cell in column_cells)
        sheet.column_dimensions[column_cells[0].column_letter].width = max_length + 2

    workbook.save(XLSX_PATH)


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    rows, alarms = build_rows()
    write_csv(rows)
    write_excel(rows)

    print(f"Generated {len(rows)} rows.")
    print(f"CSV:  {CSV_PATH}")
    print(f"XLSX: {XLSX_PATH}")
    print(f"Alarm distribution: {dict(alarms)}")


if __name__ == "__main__":
    main()
