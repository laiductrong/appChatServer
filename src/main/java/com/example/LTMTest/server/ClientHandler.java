package com.example.LTMTest.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clinetUsername;
    public String partner = "";

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public ClientHandler() {

    }

    //kiểm tra tên người dùng nhập vào
    public boolean checkName(String newName) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clinetUsername.equals(newName)) {
                return true;
            }
        }
        return false;
    }


    //gửi tin nhắn tới client xem có muốn trò chuyện với người này hay không
    public void askClient() {
        ClientHandler clientHandlerAsk = null;
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
            if (clientHandler.clinetUsername.equals(clinetUsername)) {
                clientHandlerAsk = clientHandler;
            }
        }

        try {
            if (listUserNoHavePartner.size() > 1) {
                clientHandlerAsk.bufferedWriter.write(listUserNoHavePartner.toString());
                clientHandlerAsk.bufferedWriter.newLine();
                clientHandlerAsk.bufferedWriter.flush();
                setOption();
            } else {
                clientHandlerAsk.bufferedWriter.write("ban dang trong hang doi");
                clientHandlerAsk.bufferedWriter.newLine();
                clientHandlerAsk.bufferedWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void askOtherClient(String nameOtherClient) {
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
        }
        try {
            ClientHandler clientHandler = clientHandlers.get(clientHandlers.size() - 1);
            if (clientHandler.clinetUsername.equals(nameOtherClient)) {
                clientHandler.bufferedWriter.write(listUserNoHavePartner.toString());
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //xac nhan lua chon
    public boolean isOption(String option) {
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
        }
        option = option.substring(clinetUsername.length() + 3);
        for (String s : listUserNoHavePartner) {
            if (s.equals(option)) {
                return true;
            }
        }
        return false;
    }

    //Gửi tin nhắn
    public void broadcastMessage(String messageToSend) {
        System.out.println("chuoi gui :" + messageToSend);
        for (ClientHandler clientHandler : clientHandlers) {
            //xác nhận lựa chọn
            if (clientHandler.partner.equals("") && isOption(messageToSend)) {
                String userWantChat = messageToSend.substring(clinetUsername.length() + 3);
                if (((clientHandlers.size())) > 0) {
                    for (ClientHandler clientHandler1 : clientHandlers) {
                        if (clientHandler1.clinetUsername.equals(userWantChat)) {
                            clientHandler1.partner = clinetUsername;
                            partner = userWantChat;
                        }
                    }
                }
            }
            //Kiểm tra nếu chưa có bạn chat thì cho chọn
            if (clientHandler.partner.equals("") && !isOption(messageToSend)) {
                try {
                    if (clientHandler.clinetUsername.equals(clinetUsername)) {
                        System.out.println("luwa chonn: " + messageToSend);
                        clientHandler.bufferedWriter.write("option of you is:");
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
            //Kiểm tra nếu đúng người chat chung thì send tin nhắn
            if (clientHandler.partner.equals(clinetUsername)) {
                try {
                    if (!clientHandler.clinetUsername.equals(clinetUsername)) {
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }
    }

    public void removeClientHandler() {
        //lấy danh sách hàng đợi
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
        }
        //Gửi thông báo đến đối đương khi out và set partner bằng rỗng
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals(clinetUsername)) {
                try {
                    clientHandler.bufferedWriter.write("Đối phương đã thoát, danh sách chờ :" + listUserNoHavePartner.toString());
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
                clientHandler.partner = "";

            }
        }

        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clinetUsername + " has left the chat!");
    }


    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUserName() {
        try {
            String nametemp = bufferedReader.readLine();
            if (checkName(nametemp)) { //nếu bị trùng
                bufferedWriter.write("not ok");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                setUserName();
            } else {
                this.clinetUsername = nametemp;
//                bufferedWriter.write("ok");
//                bufferedWriter.newLine();
//                bufferedWriter.flush();
//                askClient();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //lua chon cua nguoi dung
    public void setOption() {
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
        }
        {
            try {
                String option = bufferedReader.readLine();
                System.out.println("set op:" + option);
                option = option.substring(clinetUsername.length() + 3);
                System.out.println("set op 2:" + option);
                for (String s : listUserNoHavePartner) {
                    if (s.equals(option)) {
                        this.partner = option;
                        break;
                    }
                }
                for (ClientHandler clientHandler : clientHandlers) {
                    if (clientHandler.clinetUsername.equals(option)) {
                        clientHandler.partner = clinetUsername;
                        //khi co ng nhan tin thi gui thong diep toi ng con lai
                        clientHandler.bufferedWriter.write("start chat with "+clinetUsername);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void run() {
//        String messageFromClient;
        this.clinetUsername = UUID.randomUUID().toString();// usernam temp
        clientHandlers.add(this);
        setUserName();
        askClient();
        while (socket.isConnected()) {
            try {
                String messageFromClient = bufferedReader.readLine();                                      //n1 : "thong diep"
                String messTemp = messageFromClient.substring(clinetUsername.length() + 3);
                System.out.println("gui tu client string tam :" + messTemp);
                String[] list = messTemp.split(":");
                clientHandlers.stream().filter(c -> c.clinetUsername.equals(partner)).findFirst().ifPresent(clientHandler -> {
                            try {
                                clientHandler.bufferedWriter.write(messageFromClient);
                                clientHandler.bufferedWriter.newLine();
                                clientHandler.bufferedWriter.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
}
