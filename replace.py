import os

files = [
    r'androidApp/src/main/kotlin/app/rafiqaldhikr/ui/screens/adhkar/AdhkarCategoriesScreen.kt',
    r'androidApp/src/main/kotlin/app/rafiqaldhikr/ui/screens/adhkar/CelebrationScreen.kt',
    r'androidApp/src/main/kotlin/app/rafiqaldhikr/ui/screens/adhkar/DhikrReadingScreen.kt'
]

import re

for f in files:
    if not os.path.exists(f):
        print(f'File {f} not found!')
        continue
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    if 'AdhkarCategoriesScreen' in f:
        content = re.sub(r'\bC\.', 'AdhkarColors.', content)
    elif 'CelebrationScreen' in f:
        content = re.sub(r'private object C', 'private object CelebColors', content)
        content = re.sub(r'\bC\.', 'CelebColors.', content)
        content = re.sub(r'\bC\b', 'CelebColors', content) # Ensure object usages are caught
    elif 'DhikrReadingScreen' in f:
        content = re.sub(r'private object C', 'private object ReadColors', content)
        content = re.sub(r'\bC\.', 'ReadColors.', content)
        content = re.sub(r'\bC\b', 'ReadColors', content)

    with open(f, 'w', encoding='utf-8') as file:
        file.write(content)

print("Replacement successful!")
