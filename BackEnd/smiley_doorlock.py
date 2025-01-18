#if문으로 코드 다 합친 것

from turtle import distance
from uuid import uuid4
import dlib
import cv2
import numpy as np
import os
from math import hypot
import time
import RPi.GPIO as GPIO
from datetime import datetime
from datetime import datetime
import datetime
#firebase 연동을 위한 import
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import firestore
from firebase_admin import storage
#LCD 사용을 위한 import 
from RPLCD import CharLCD


#firebase 'doorlock'의 database와 storage 연동
PROJECT_ID = "doorlock-1e03d"
cred = credentials.Certificate("/home/pi/Desktop/doorlock-1e03d-firebase-adminsdk-4ymw3-c580f90c8f.json")
default_app = firebase_admin.initialize_app(cred,
{'storageBucket' : 'doorlock-1e03d.appspot.com'
,'databaseURL':'https://doorlock-1e03d-default-rtdb.firebaseio.com/'
}
)

ref=db.reference()


#landmark를 이용하여 얼굴을 찾아내는 함수 
def find_faces (image) :

    faces = detector(image, 1)
    #image에서 얼굴을 1개 인식하여 face로 설정

    if len(faces) == 0:

        return np.empty(0)

    rects, lands = [],[]

    lands_np = np.zeros((len(faces), 68, 2), dtype=np.int)

    for k, d in enumerate(faces):

        rect = ( (d.left(), d.top()), (d.right(), d.bottom()) )

        rects.append(rect)

        land = predictor(image, d)

        lands.append(land)

        for i in range(0, 68):

            lands_np[k][i] = (land.part(i).x, land.part(i).y)
            

    return rects, lands, lands_np


def encode_faces(image, lands):

    face_descriptors = []

    for land in lands:

        face_descriptor = face_recog.compute_face_descriptor(image, land)

        face_descriptors.append(np.array(face_descriptor))

    return np.array(face_descriptors)



    
detector = dlib.get_frontal_face_detector()
    #face detector 정면

predictor = dlib.shape_predictor("shape_predictor_68_face_landmarks.dat")
    #랜드마크 68개 점

face_recog = dlib.face_recognition_model_v1("/home/pi/Desktop/dlib_face_recognition_resnet_model_v1.dat")
    #얼굴 인지 비교

faceDescs_list = os.listdir('faceDescs/') #faceDescs안에 있는 데이터 전부 가져오기


#연결된 라즈베리파이 카메라로 촬영되는 모습을 cam으로 설정
cam = cv2.VideoCapture(0)

cnt=0




#text lcd 설정
lcd = CharLCD(cols=16, rows=2, pin_rs=25, pin_e=26, pins_data=[13, 17, 18, 22], numbering_mode=GPIO.BCM)

#출입기록 시에 firebase에 촬영된 사진을 업로드하기 위한 함수
def fileUpload():
    bucket = storage.bucket() 
    blob = bucket.blob("Visitors/"+str(filename)+".jpg") 
    #라즈베리파이의 Visitors 폴더에 filename 변수의 이름으로 저장된 jpg 사진 파일을 가져옴
    new_token = uuid4
    metadata = {"firebaseStorageDownloadTokens": new_token}
    blob.metadata = metadata
    blob.upload_from_filename("Visitors/"+str(filename)+".jpg")
    #앞에서 가져온 사진 파일을 firebase storage의 출입기록을 위한 폴더 Visitors에 filename변수의 이름으로 jpg 파일 업로드
    print("사진 업로드") 
    print(blob.public_url)
    ref=db.reference('Visit')
    ref.push({'time':filename, 'fy':fy})  #Android 출입기록 연동을 위한 database의 time(출입시도한 시간)과 fy(사용자 등록 여부) 변수를 업데이트


TRIG = 23
ECHO = 24
#초음파 센서 설정

GPIO.setmode(GPIO.BCM) 
GPIO.setwarnings(False)  
GPIO.setup(14,GPIO.OUT) #도어락 핀 설정
GPIO.setup(TRIG, GPIO.OUT) 
GPIO.setup(ECHO, GPIO.IN) #초음파 센서 핀 설정
GPIO.setup(2, GPIO.IN) #화재감지 센서 핀 설정



try:
    while(True):

      ref=db.reference()
      ref_name=db.reference('name')
      name=ref_name.get()
      #name : 사용자 정보에서의 이름 변수, 사용자 등록 및 삭제와 파일 업로드 시 사용
      #firebase realtime database의 name 변수를 가져와 name에 저장

      ref=db.reference()
      ref_remove=db.reference('remove')
      remove=ref_remove.get()
      #remove : 안드로이드에서 사용자가 사용자 정보 삭제 버튼을 눌렀을 때 바뀌는 변수 
      #firebase realtime database의 remove 변수를 가져와 remove에 저장


      ref=db.reference()
      ref_save=db.reference('save')
      save=ref_save.get()
      #save : 안드로이드에서 사용자가 정보 등록 시 저장 버튼을 눌렀을 때 바뀌는 변수
      #firebase realtime database의 save 변수를 가져와 save에 저장

      ref=db.reference()
      ref_fp=db.reference('fp')
      fp=ref_fp.get()
      #fp : 안드로이드 앱에서 사용자가 도어락 제어를 시도했을 때 지문 인식에 성공하면 바뀌는 변수
      #firebase realtime database의 fp 변수를 가져와 fp에 저장

      lcd.clear()
      lcd.cursor_pos=(0,0)
      lcd.crlf()
      lcd.write_string('start')
      #lcd에 코드 실행을 알리는 문구 출력

      #초음파 거리 측정
      GPIO.output(TRIG, True)
      time.sleep(0.00001)
      GPIO.output(TRIG, False)
      print("초음파 거리 측정 중 . . .")

      while GPIO.input(ECHO)==0:
        pulse_start = time.time()
    #echo핀 상승 시간값 저장

      while GPIO.input(ECHO)==1:
       pulse_end = time.time()
    #echo핀 하강 시간값 저장

      rtTotime = pulse_end - pulse_start
      distance = rtTotime * ( 34000 / 2)
      print("distance: %.2f cm " %distance)
  
  
      #       <<< 1. 얼굴 인식 및 출입 기록>>>
      if distance <= 40: #초음파 센서와의 거리가 25cm 이하로 측정 되면 ""얼굴 인식""" 기능 실행
        ret, img_o = cam.read()
        img = cv2.flip(img_o, -1)
        if not ret:
          break
        image = cv2.resize(img, dsize=(200, 200), interpolation=cv2.INTER_AREA)
        img_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        #카메라 화면 설정을 위한 코드
        
        lcd.clear()
        lcd.cursor_pos=(0,6)
        lcd.write_string('detect')
        #lcd에 얼굴을 인식 중이라는 텍스트 출력

        faces = detector(image, 1)
    
        if faces: #얼굴이 감지가 되면
          print("얼굴 감지")
          now = datetime.datetime.now() 
          filename = now.strftime('%Y%m%d%H%M%S')
          cv2.imwrite("Visitors/"+ str(filename) +".jpg", image)
          #얼굴이 감지 됐을 때의 시간을 파일 이름으로 하여 카메라에 찍힌 모습을 라즈베리파이 Visitors 폴더에 저장
          print("출입 기록 저장 완료")  
        
          last_found = {'name': 'unknown', 'dist': 0.4, 'color': (0,0,255)}


        for k, d in enumerate(faces):
            #사각형위치 찾아놓기
            rect = ( (d.left(), d.top()), (d.right(), d.bottom()) )
        
            landmarks = predictor(image, d)
 
            face_descriptor = face_recog.compute_face_descriptor(image, landmarks)
        
            for i in range(len(faceDescs_list)):
                descs = np.load('faceDescs/' + faceDescs_list[i], allow_pickle=True)[()]
                #npy파일 열어서 descs로 저장
                #faceDescs = 등록된 사용자의 얼굴 인식 정보값 배열 파일을 저장하는 폴더

                for name, saved_desc in descs.items():

                  dist = np.linalg.norm([face_descriptor] - saved_desc, axis=1)
                    #유클리디안 거리계산
                    #인식된 얼굴의 점과 등록된 사용자의 점의 사이의 거리 계산
                    
                  if dist >= 0.4: #거리가 0.4 이상일 때 = 등록된 사용자가 아닐 때
                        last_found = {'name': name, 'dist': dist, 'color': (255,255,255)}
                        lcd.clear()
                        print("등록된 사용자가 아닙니다.")
                        lcd.cursor_pos=(0,3)
                        lcd.write_string("not user")
                        #LCD에 등록된 사용자가 아님을 출력
                        time.sleep(3)
                        fy = 'false'
                        #사용자 등록 여부를 나타내는 변수인 fy에 false를 저장
                        fileUpload()
                        #firebase storage에 사진과 출입기록에 관한 변수를 업데이트 하는 함수 실행
                        break
             
                  if dist < 0.4: # 거리가 0.4 이하일 때 = 등록된 사용자일 때
                    lcd.clear()
                    lcd.cursor_pos=(0,3)
                    lcd.write_string("user:")
                    lcd.cursor_pos=(1,3)
                    lcd.write_string(name)
                    #LCD로 사용자 id 출력
                    last_found = {'name': name, 'dist': dist, 'color': (255,255,255)}
                    cv2.rectangle(image, pt1=(d.left(), d.top()), pt2=(d.right(), d.bottom()), color=last_found['color'], thickness=2)
                    cv2.putText(image, last_found['name'], org=(d.left(), d.top()), fontFace=cv2.FONT_HERSHEY_SIMPLEX, fontScale=1, color=last_found['color'], thickness=2)
                    print("사용자 :"+ name )
                    print("문이 열렸습니다.")
                    fy = 'true'
                    #사용자 등록 여부를 나타내는 변수인 fy에 ture를 저장
                    fileUpload()
                    #firebase storage에 사진과 출입기록에 관한 변수를 업데이트 하는 함수 실행
                    GPIO.output(14,GPIO.HIGH)
                    time.sleep(3)
                    GPIO.output(14,GPIO.LOW)
                    #도어락 제어
                    lcd.clear()
                    lcd.cursor_pos=(0,3)
                    lcd.write_string("door")
                    lcd.cursor_pos=(1,3)
                    lcd.write_string("open")
                    #LCD에 문이 열렸다는 텍스트 출력
                    time.sleep(3)
                    break 
                break
                
      
                
      #           <<< 2. 등록된 사용자 정보 삭제 >>>
      if remove == "On": #안드로이드 앱에서 사용자가 삭제 버튼을 눌렀을 때 = remove 변수가 On이 되면
        print("삭제 실행")
        r_name = name + '.npy' #안드로이드에서 선택한 사용자 정보의 name 값으로 된 npy 파일 이름 설정
        r_file = ('faceDescs/' + r_name) #해당 파일 이름으로 된 파일을 faceDescs (배열값 저장 폴더)에서 찾아냄
        print("r_file:"+r_name) 
        os.remove(r_file) #해당 배열 파일 삭제 
        ref_remove.update({'remove':'Off'}) #remove 변수 변경
        print("삭제 완료")


      #         <<< 3. 새로운 사용자 정보 등록 >>>
      if save == "On": #안드로이드 앱에서 사용자가 저장 버튼을 눌렀을 떄 = save 변수가 On이 되면
        bucket=storage.bucket()
        blob=bucket.blob("uploads/"+ name +".jpg") #firebase storage의 uploads/ 폴더에서 name으로 된 사진 파일 선택
        time.sleep(5)
        blob.download_to_filename("OriginData/"+ name +".jpg") #선택한 파일 라즈베리파이의 OriginData 폴더로 다운로드
        time.sleep(5) 
        print("저장 완료")
        print("사용자 : " + name)
        ref_save.update({'save':'Off'}) #저장 후 save 변수 update
        time.sleep(5)

        file_name = "OriginData/" + name + ".jpg"

        image = cv2.imread(file_name)


        detected_faces = detector(image, 1) #다운받은 사진파일 열어 얼굴 인식

        image_path = (r"OriginData/")
        image_paths = {name : image_path}
        img_num = 1
        descs = {name : None} 

        for name, image_path in image_paths.items():#name = userName, image_path = faceData/
                    for idx in range (img_num):
                       img_p = image_path + name + '.jpg'
                       img_bgr = cv2.imread(img_p)
                       img_rgb = cv2.cvtColor(img_bgr,cv2.COLOR_BGR2RGB)
                #저장된 사진에서 얼굴 찾기
                       _, img_lands, _ = find_faces(img_rgb)
                #userName에 인코딩값을 넣기
                       descs[name] = encode_faces(img_rgb, img_lands)
                       print("얼굴등록이 완료되었습니다.")
                       #인코딩파일 저장
                       np.save('faceDescs/' + name + '.npy', descs)
      

      #         <<< 4. 화재 감지 센서 작동 >>>
      if GPIO.input(2) == True: #화재감지센서에 불꽃이 감지되었을 때
          now = datetime.datetime.now()
          cur_date_time = now.strftime('%Y-%m-%d %H:%M:%S') #감지된 시간 값을 년월일시분초로 가져옴
          print("Fire Detected")
          print(cur_date_time)
          lcd.clear()
          lcd.cursor_pos=(0,0)
          lcd.crlf()
          lcd.write_string("fire\ndetected")
          ref.update({'message':'Fire Detected'})
          ref.update({'fire_time':cur_date_time})
          #안드로이드 앱에 알림 전송을 위해 화재 감지 시간과, 화재 감지 메시지 변수 update
          time.sleep(3)


      #         <<< 5. 도어락 연동 제어를 위한 지문 인식 >>>
      if fp == "success": #안드로이드 앱에서 지문인식에 성공했을 때 
        GPIO.output(14,GPIO.HIGH)
        time.sleep(5)
        GPIO.output(14,GPIO.LOW)
        #도어락 제어
        print("door open")
        lcd.clear()
        lcd.cursor_pos=(0,0)
        lcd.crlf()
        lcd.write_string("open")
        #LCD에 문이 열렸음을 알리는 문구 출력
        ref.update({'fp':"fail"}) #firebase 지문 인식 변수 update






except KeyboardInterrupt:
  GPIO.cleanup()
