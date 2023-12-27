import random

from moviepy.editor import *


def overlay_audio_and_image(video_file, video_begin, audio_file, image, path):
    video = path + video_file

    duration = 10

    video_clip = VideoFileClip(video).subclip(video_begin, video_begin + duration)
    audio_clip = AudioFileClip(audio_file).subclip(0, duration)

    image_array = imageio.imread(image)

    image_clip = ImageClip(image_array, duration=video_clip.duration, transparent=True)
    resized_image_clip = image_clip.resize(width=750, height=750)

    video_clip = video_clip.set_audio(audio_clip)

    sub_image_clip = ImageClip(path + 'subscribe.png', duration=video_clip.duration, transparent=True)

    video_with_image = CompositeVideoClip([video_clip.resize(width=1080, height=1920)
                                          .set_position('center').set_duration(video_clip.duration),
                                           resized_image_clip.set_position('center')], size=(1080, 1920))

    completed_video = CompositeVideoClip([video_with_image.set_duration(video_with_image.duration),
                                          sub_image_clip.set_position('bottom').resize(width=600, height=600)])

    output = path + 'completed\\' + ''.join([str(random.randint(0, 9)) for _ in range(10)]) + '.mp4'

    # Сохраняем результат
    completed_video.write_videofile(output, codec='libx264', audio_codec='aac')


args = sys.argv

video_info = args[1]
parts = video_info.split("+")
video_file_name = parts[0]
begin = int(parts[1])

audio_file_name = args[2]
src_meme = args[3]
directory_path = args[4]

audio_path = directory_path + 'songs\\' + audio_file_name

overlay_audio_and_image(video_file_name, begin, audio_path, src_meme, directory_path)
