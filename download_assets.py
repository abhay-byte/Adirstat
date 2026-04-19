import os
import re
import urllib.request
import hashlib

def main():
    assets_dir = 'assets/design_assets'
    os.makedirs(assets_dir, exist_ok=True)
    
    # Regex to find src="https://..."
    url_pattern = re.compile(r'src="(https://lh3.googleusercontent.com/[^"]+)"')
    
    html_files = []
    for root, dirs, files in os.walk('stitch_storage_analyzer_adirstat'):
        for file in files:
            if file.endswith('.html'):
                html_files.append(os.path.join(root, file))
                
    url_to_path = {}
    
    for html_file in html_files:
        with open(html_file, 'r', encoding='utf-8') as f:
            content = f.read()
            
        urls = url_pattern.findall(content)
        for url in urls:
            if url not in url_to_path:
                filename = hashlib.md5(url.encode()).hexdigest()[:8] + '.png'
                filepath = os.path.join(assets_dir, filename)
                
                print(f"Downloading {url[:40]}... to {filepath}")
                try:
                    # Using a generic User-Agent to avoid HTTP 403 Forbidden
                    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
                    with urllib.request.urlopen(req) as response, open(filepath, 'wb') as out_file:
                        out_file.write(response.read())
                    url_to_path[url] = filepath
                except Exception as e:
                    print(f"Failed to download {url[:40]}: {e}")
                    
    # Now replace the URLs in the HTML files
    for html_file in html_files:
        with open(html_file, 'r', encoding='utf-8') as f:
            content = f.read()
            
        modified = False
        for url, filepath in url_to_path.items():
            if url in content:
                # Calculate relative path from html file to the asset
                rel_path = os.path.relpath(filepath, os.path.dirname(html_file))
                content = content.replace(url, rel_path)
                modified = True
                
        if modified:
            with open(html_file, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated {html_file}")

if __name__ == '__main__':
    main()
