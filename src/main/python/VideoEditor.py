from moviepy.audio.io.AudioFileClip import AudioFileClip
from moviepy.editor import VideoFileClip, ImageClip, CompositeVideoClip, CompositeAudioClip
import sys


def overlay_audio_and_image(video, audio, image, output):
    # Загрузка видео, аудио и изображения
    video_clip = VideoFileClip(video)
    audio_clip = AudioFileClip(audio).subclip(0, video_clip.duration)
    image_clip = ImageClip(image, duration=video_clip.duration)

    # Наложение аудио на видео
    video_clip = video_clip.set_audio(CompositeAudioClip([video_clip.audio, audio_clip]))

    # Наложение изображения на видео
    video_with_image = CompositeVideoClip([video_clip, image_clip])

    # Сохранение результата
    video_with_image.write_videofile(output, codec='libx264', audio_codec='aac')


args = sys.argv
file_name = args[1]
video_path = 'D:\\videos\\' + file_name
audio_path = 'D:\\videos\\songs\\' + args[2]
image_path = 'D:\\videos\\completed\\sample.png'
output_path = 'D:\\videos\\completed\\' + file_name

overlay_audio_and_image(video_path, audio_path, image_path, output_path)
