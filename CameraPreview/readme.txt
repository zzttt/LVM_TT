* jni 디렉토리 구조 *
jni : ndk c소스가 담겨있는 디렉토리, 프로젝트 빌드옵션에 의해 소스 수정후 refresh하면 자동으로 컴파일됨
libs : jni에서 컴파일된 라이브러리가 담겨지는 폴더




* jni 함수 추가 및 사용법 *
ProcessCore Calss에서
private native int Upper(Bitmap _outBitmap, byte[] _in, int resolution);
형태로 native 함수를 정의한후 터미널에서
{프로젝트 디렉토리} / bin/classes/ 로 이동한다.
javah -classpath ~/android/adt-bundle-linux-x86-20130219/sdk/platforms/android-16/android.jar: com.androidhuman.example.CameraPreview.ProcessCore
로 명령어를 내리면 jni를 위한 헤더파일이 생성된다.
(타이핑이 귀찮으면 그대로 복사후 Shift + Insert를 누르면 붙여넣기가 된다.)
이렇게 만들어진 .h 헤더파일을 jni 폴더로 복사 후 native_proc.c 에서 함수를 추가하여 사용하면 된다.

주의 : 파일명을 함부로 바꾸면 컴파일이 안된다.
파일명을 바꾸고 싶으면
Android.mk 에서 컴파일 옵션과 파일명을 정의하고 있다.
Target Deivce를 바꾸고 싶으면 Application.mk에서 설정파일이 들어있다.




* class 개요 *
이 소스에는 layout을 위한 xml파일이 하나도 없다(...)
xml이 없으면 xml문서를 위한 파서도 동작할일이 없기때문에 성능적인 면에서 이득을 볼 수 있으나
layout을 관리하는데 지옥이 펼쳐진다....ㅠ
사실은 본인의 실력이 부족하여 xml형태로 짤 수 없었던것이지만 그려려니 하고 넘어가자(....ㅈㅅ)
xml을 없고, 화면 구성을 위한 layout은 모두 CameraPreview에서 하고있다.

CameraPreview : 실질적인 Main Activity. 전체적인 화면 구성이 정의되어있음
Controller : 현재 사용하지 않음. 지워도 무방. 그냥 빈 Activity
DrawOnTop : 네모 박스나, 문자열 등등 그려지는 이미지들
MainActivity : 로딩(Splash)화면 보여주기용 
Pop : Help메세지용 팝업 //어디서 긁어온소스라 아직 분석안함
PopView : Pop의 형태를 저장하고있는 class //어디서 긁어온소스라 아직 분석안함
ProcessCore : SurfaceView와 실질적인 처리 알고리즘이 들어있는 class
SplashActivity : 현재 사용하지 않음. 지워도 무방


