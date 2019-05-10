# snoring_detect

1. 코골이 소리를 입력
2. 코골이 소리에서 spectogram을 추출해서 코골이 인지 체크하는 로직 수행
2-1. 수행 중 오류 발생 arraycopy 오류
-> 코골이를 위한 lowerboundary, upperboundary 등을 알아야 해서, spectrum 값에 맞게 임의 값을 입력함
3. audio 파일 전체를 비교하고 있던 로직을 기존 로직과 동일하게 buffer 단위로 읽고 체크하도록 수정
4. fft 체크 로직과 spectogram 체크 로직을 분리했음.
5. fft 체크 로직에서는 snoring으로 탐지가 되지만, spectogram 체크 로직에서는 탐지가 안되는 코골이 사운드가 발생.
6. 라이브러리는 8, 16 비트만 지원해서 다른 사운드 테스트 불가


포먼트의 최대값 주파수 가져오기
1. FFT 로직 이용한 포먼트 정보 가져오기
2. FFT 로직 이용한 max(포먼트) 가져오기
3. FFT에서 탐지되는 주파수는 오디오의 sample rate마다 다르다. -> 44100으로만 사용하기로 함.
-> 1번의 포먼트 정보 메소드 및 오디오 편집 툴에서 sample rate마다 최대 탐지되는 주파수가 다름을 확인했음.
4. 3번에서 편집툴로 확인해보고나서, FFT는 0.01초간격으로 0~22050Hz(44100 sample rate 기준)의 각 Hz별 magnitude(db, 신호세기)표현하는 정보임을 알게됨
-> 즉, 포먼트는 0.01초 간격으로 측정되는 주파수별 신호세기이며, 현재 구현가능한 로직은 가장 높은 주파수를 측정하는 것만 가능하다.
5. 4번의 내용으로는 논문에서 200Hz의 narrow peak과 1000Hz의 wide peak이 발생하는 케이스를 발견할 수 없다. 오직 가장 높은 Hz만 얻을 수 있기 때문.
-> fft에서 200Hz와 1000Hz의 peak width(넓은정도?)를 구할 수 있는 로직이 필요함.
6. peak width는 amplitude로 표현됨.
-> 가장 높은 hz, db, amplitude를 가져올 순 있으나, 200hz와 1000hz의 값만 특정하는 로직은 찾지 못함.

7. 가장 높은 peak이 190~210인것을 찾고 두번째 높은 peak이 990~1010인 peak의 카운트를 체크한다.
8. 코골이랑 발걸음이 체크된다.

※ bitRate마다 오디오의 시간이 다르다. -> 오디오 스트림에서 읽어오는 버퍼의 크기는 1024, 즉 0.01초 였음.
https://sound.stackexchange.com/questions/42569/how-to-get-number-of-framesor-samples-per-sec-or-ms-in-a-audio-wav-or-mp3
byte가 3,974,200인 파일 기준 계산했을 때 45.05~값이 정확하게 나옴.
byte가 1024이면 0.01초..
length = byte / bit rate * 8
bit rate = sample rate * bit depth * channels
sample rate = 44100, bit depth = 16, channels = 1 로 고정
-> 왜 0.01초 인가, FFT 계산식에서 time resolution을 0.01로 고정하고 있기 때문에, byte를 1024(0.01초)씩 가져오는게 맞음.
https://kr.mathworks.com/help/signal/ug/fft-based-time-frequency-analysis.html

☞ 이갈이랑 무호흡을 확인해야 한다.‬
