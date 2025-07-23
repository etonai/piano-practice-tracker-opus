import csv

input_file = 'app/docs/PlayTest_2024.csv'
output_file = 'app/docs/PlayTest_large.csv'

def change_year(row, old_year, new_year):
    if row and row[0].startswith(old_year):
        row = [row[0].replace(old_year, new_year, 1)] + row[1:]
    return row

def main():
    with open(input_file, newline='', encoding='utf-8') as infile:
        reader = list(csv.reader(infile))
        header = reader[0]
        data = reader[1:]

    with open(output_file, 'w', newline='', encoding='utf-8') as outfile:
        writer = csv.writer(outfile)
        writer.writerow(header)
        # Write 2024 data
        writer.writerows(data)
        # Write 2023 data
        for row in data:
            writer.writerow(change_year(row, '2024', '2023'))

if __name__ == '__main__':
    main() 