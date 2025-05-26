from PIL import Image, ImageDraw
import os

# 아이콘 크기들
sizes = [(48, 'mdpi'), (72, 'hdpi'), (96, 'xhdpi'), (144, 'xxhdpi'), (192, 'xxxhdpi')]

for size, density in sizes:
    # 새 이미지 생성 (오렌지 배경)
    img = Image.new('RGBA', (size, size), (255, 179, 102, 255))
    draw = ImageDraw.Draw(img)
    
    # 중앙 좌표
    center = size // 2
    
    # 두뇌 모양 (타원)
    brain_size = int(size * 0.4)
    brain_left = center - brain_size // 2
    brain_top = center - brain_size // 2
    brain_right = center + brain_size // 2
    brain_bottom = center + brain_size // 2
    
    # 두뇌 그리기 (분홍색)
    draw.ellipse([brain_left, brain_top, brain_right, brain_bottom], 
                fill=(255, 107, 107, 255), outline=(44, 62, 80, 255), width=2)
    
    # 중앙선
    draw.line([center, brain_top, center, brain_bottom], fill=(44, 62, 80, 255), width=2)
    
    # 덤벨 (간단한 직사각형들)
    dumbbell_width = int(size * 0.15)
    dumbbell_height = int(size * 0.05)
    
    # 왼쪽 덤벨
    left_x = brain_left - dumbbell_width
    left_y = center - dumbbell_height // 2
    draw.rectangle([left_x, left_y, left_x + dumbbell_width, left_y + dumbbell_height], 
                  fill=(44, 62, 80, 255))
    
    # 오른쪽 덤벨
    right_x = brain_right
    right_y = center - dumbbell_height // 2
    draw.rectangle([right_x, right_y, right_x + dumbbell_width, right_y + dumbbell_height], 
                  fill=(44, 62, 80, 255))
    
    # 저장
    folder = f'app/src/main/res/mipmap-{density}'
    os.makedirs(folder, exist_ok=True)
    img.save(f'{folder}/ic_launcher.png')
    img.save(f'{folder}/ic_launcher_round.png')

print('아이콘 생성 완료!') 