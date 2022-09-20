package com.douzone.fileapp.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {
    public static void main(String[] args) {
        while (true) {
            run();
        }
    }

    private static void run() {
        String path = "./server/";
        /**
         * Try with Resources 로 Auto Closable.
         */
        try (ServerSocket serverSocket = new ServerSocket(5555);
             Socket socket = serverSocket.accept();
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        ) {

            System.out.println("연결됨 : " + socket.getRemoteSocketAddress());
            StringBuilder stringBuilder = getFilelist(path);
            dataOutputStream.writeUTF(String.valueOf(stringBuilder));
            FileInputStream fileInputStream = null;
            try {
                String fileName = dataInputStream.readUTF();
                fileInputStream = new FileInputStream(path + fileName);
                dataOutputStream.writeUTF("200");
                System.out.println(socket.getRemoteSocketAddress() + "에게 " + fileName + " 파일을 전송합니다.");
                byte[] buffer = new byte[8192];
                while (true) {
                    int count = fileInputStream.read(buffer);
                    if (count == -1) {
                        break;
                    }

                    /**
                     * void write( byte[] b , int off , int len )
                     * - b배열 안에 있는 시작 off(index) 부터 len 만큼 출력한다.
                     * 출처: https://zzdd1558.tistory.com/155 [YundleYundle:티스토리]
                     */
                    dataOutputStream.write(buffer, 0, count);
                }
                System.out.println(socket.getRemoteSocketAddress() + "에게 " + fileName + " 파일을 전송했습니다.");
            } catch (FileNotFoundException e) {
                dataOutputStream.writeUTF("404");
            }
            /**
             * 그 이유는 바로 효율성 때문이다. 첫번쨰 코드에서 while 문을 돌려보면 바이트 수만큼 돌아간다. 그러니 예를 들어 우리가 파일에다가 1024자를 쓰면 while 문을 1024번돌고 이는 우리가 한글자마다 읽고,
             * stream에 넣고 ,stream에 있는걸 read로 빼내고 이 과정을 반복한다는 것이다. 그리고 이는 상식적으로 매우 비효율적이다.
             * 그래서 우리가 배열이라는 개념을 여기다가 적용하면 1024자라는 글자를 한번에 배열에다가 저장할수가 있다. 그래서 배열을 이용한다.
             * 이개념은 향후에 buffer 라는 것을 배울때 익히는 중요한 기본원리이니 반드시 기억해두도록하자.
             * https://mainpower4309.tistory.com/20
             */
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static StringBuilder getFilelist(String path) {
        File file = new File(path);
        String[] fileList = file.list();
        StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("현재 파일 목록 \n");
        for (String fileName : fileList) {
            stringBuilder.append(fileName).append("\n");
        }
        return stringBuilder;
    }
}
