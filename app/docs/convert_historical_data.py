#!/usr/bin/env python3
"""
Convert historical practice log CSV to Piano Track Opus format
"""

import csv
import datetime
from typing import Dict, List, Tuple

def parse_date(date_str: str) -> str:
    """Convert YYYYMMDD to YYYY-MM-DD format"""
    if len(date_str) == 8:
        year = date_str[:4]
        month = date_str[4:6]
        day = date_str[6:8]
        return f"{year}-{month}-{day}"
    return date_str

def map_activity_code(code: str) -> Tuple[str, int, str]:
    """
    Map activity codes to (activity_type, level, performance_type)
    Returns: (PRACTICE/PERFORMANCE, level, performance_type)
    """
    # Remove any trailing characters like V, ?, etc.
    clean_code = ''.join(c for c in code if c in 'pPaAXx')
    
    if not clean_code:
        return None
        
    # Performance codes
    if 'X' in clean_code:
        return ("PERFORMANCE", 2, "practice")  # Satisfactory
    elif 'x' in clean_code:
        return ("PERFORMANCE", 1, "practice")  # Unsatisfactory
    
    # Practice codes (check for multiple, take highest level)
    practice_levels = []
    if 'P' in clean_code:
        practice_levels.append(4)  # Perfect Complete
    if 'A' in clean_code:
        practice_levels.append(2)  # Complete with Review
    if 'p' in clean_code or 'a' in clean_code:
        practice_levels.append(1)  # Essentials
    
    if practice_levels:
        return ("PRACTICE", max(practice_levels), "practice")
    
    return None

def convert_csv():
    """Convert the historical CSV to app format"""
    input_file = "Piano Songs Performance Record - Sheet1.csv"
    output_file = "historical_piano_data.csv"
    
    # Output CSV headers matching the app's export format
    output_headers = [
        "DateTime",
        "Length",
        "ActivityType", 
        "Piece",
        "Level",
        "PerformanceType",
        "Notes"
    ]
    
    activities = []
    pieces_seen = set()
    
    # Read the input CSV
    with open(input_file, 'r', encoding='utf-8') as f:
        reader = csv.reader(f)
        rows = list(reader)
    
    # Get date headers (skip first empty column)
    date_headers = rows[0][1:]
    
    # Process each piece (skip header row)
    for row_idx, row in enumerate(rows[1:], 1):
        if not row or not row[0].strip():  # Skip empty rows
            continue
            
        piece_name = row[0].strip()
        if not piece_name:
            continue
            
        pieces_seen.add(piece_name)
        
        # Process each date column
        for col_idx, activity_code in enumerate(row[1:]):
            if not activity_code or activity_code.strip() == '':
                continue
                
            # Skip non-date columns like "12 days", "4 Days"
            date_str = date_headers[col_idx].strip()
            if not date_str.isdigit() or len(date_str) != 8:
                continue
                
            # Parse the activity
            activity_data = map_activity_code(activity_code.strip())
            if not activity_data:
                continue
                
            activity_type, level, performance_type = activity_data
            
            # Create timestamp (1 PM on the given date)
            date_formatted = parse_date(date_str)
            timestamp = f"{date_formatted} 13:00:00"
            
            # Add activity in correct format: DateTime,Length,ActivityType,Piece,Level,PerformanceType,Notes
            activities.append([
                timestamp,
                "-1",  # Length (no time tracking)
                activity_type,
                piece_name,
                level,
                performance_type,
                ""    # Notes
            ])
    
    # Sort activities by timestamp
    activities.sort(key=lambda x: x[0])
    
    # Write output CSV
    with open(output_file, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(output_headers)
        writer.writerows(activities)
    
    print(f"Conversion complete!")
    print(f"- Found {len(pieces_seen)} unique pieces")
    print(f"- Generated {len(activities)} activities")
    print(f"- Date range: {min(a[0] for a in activities)} to {max(a[0] for a in activities)}")
    print(f"- Output written to: {output_file}")
    
    # Show piece breakdown
    print(f"\nPieces found:")
    for piece in sorted(pieces_seen):
        piece_activities = [a for a in activities if a[1] == piece]
        practice_count = len([a for a in piece_activities if a[4] == "PRACTICE"])
        performance_count = len([a for a in piece_activities if a[4] == "PERFORMANCE"])
        print(f"  {piece}: {practice_count} practices, {performance_count} performances")

if __name__ == "__main__":
    convert_csv()