import csv

input_file = 'app/docs/PlayTest_2024.csv'
output_file = 'app/docs/PlayTest_XL.csv'
years = [2024, 2023, 2022, 2021, 2020]

def change_year(row, old_year, new_year):
    if row and row[0].startswith(str(old_year)):
        row = [row[0].replace(str(old_year), str(new_year), 1)] + row[1:]
    return row

def main():
    with open(input_file, newline='', encoding='utf-8') as infile:
        reader = list(csv.reader(infile))
        header = reader[0]
        data = reader[1:]

    with open(output_file, 'w', newline='', encoding='utf-8') as outfile:
        writer = csv.writer(outfile)
        writer.writerow(header)
        for year in years:
            for row in data:
                writer.writerow(change_year(row, years[0], year))

if __name__ == '__main__':
    main() 