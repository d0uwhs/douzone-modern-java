package com.douzone.fileapp.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientApplication {
    public static void main(String[] args) {
        try (
                Scanner scanner = new Scanner(System.in);
                Socket socket = new Socket("localhost", 5555);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            String fileList = dataInputStream.readUTF();
            System.out.println(fileList);

            System.out.println("파일명을 입력하세요.");
            String fileName = scanner.nextLine();
            dataOutputStream.writeUTF(fileName);

            String code = dataInputStream.readUTF();

            if (code.equals("404")) {
                throw new FileNotFoundException();
            }

            if (code.equals("200")) {
                FileOutputStream fileOutputStream = new FileOutputStream("./download/" + fileName);
                byte[] bytes = new byte[8192];
                while (true) {
                    int count = dataInputStream.read(bytes);
                    if (count == -1) {
                        break;
                    }
                    fileOutputStream.write(bytes, 0, count);
                }
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
