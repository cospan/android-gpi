/* Author: David McCoy dave.mccoy@cospandesign.com
 *
 *     This file is part of Android GPI.
 *
 *  Android GPI is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Android GPI is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Android GPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 */


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cospandesign.android.gpi.lcm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

import lcm.lcm.TCPProvider;
import lcm.lcm.TCPService;

/**
 *
 * @author Origionally taken from lcm's TCPService
 */
public class SubServTCPService {
    ServerSocket serverSocket;
    AcceptThread acceptThread;
    TCPBackgroundServiceCallback mCallback;
    ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
    
    int bytesCount = 0;
    
    public SubServTCPService (int port, TCPBackgroundServiceCallback callback) throws IOException {
        mCallback = callback;
        serverSocket = new ServerSocket(port   );
        
        acceptThread = new AcceptThread();
        acceptThread.start();

    }

    public void relay(byte channel[], byte data[])
    {
        // synchronously send to all clients.
        String chanstr = new String(channel);
        synchronized(clients) {
            for (ClientThread client : clients) {
                client.send(chanstr, channel, data);
            }
        }
    }

    class AcceptThread extends Thread
    {
        public void run()
        {
            while (true) {
                try {
                    Socket clientSock = serverSocket.accept();

                    ClientThread client = new ClientThread(clientSock);
                    client.start();

                    synchronized(clients) {
                        clients.add(client);
                    }
                    synchronized(mCallback ){
                        mCallback.clientConnecting(clients.size());
                    }
                } catch (IOException ex) {
                }
            }
        }
    }

    class ClientThread extends Thread
    {
        Socket sock;
        DataInputStream ins;
        DataOutputStream outs;

        class SubscriptionRecord
        {
            String  regex;
            Pattern pat;
            SubscriptionRecord(String regex) {
                this.regex = regex;
                this.pat = Pattern.compile(regex);
            }
        }

        ArrayList<SubscriptionRecord> subscriptions = new ArrayList<SubscriptionRecord>();

        public ClientThread(Socket sock) throws IOException
        {
            this.sock = sock;

            ins = new DataInputStream(sock.getInputStream());
            outs = new DataOutputStream(sock.getOutputStream());

            outs.writeInt(TCPProvider.MAGIC_SERVER);
            outs.writeInt(TCPProvider.VERSION);
        }

        public void run()
        {
            ///////////////////////
            // read messages until something bad happens.
            try {
                while (true) {
                    int type = ins.readInt();
                    if (type == TCPProvider.MESSAGE_TYPE_PUBLISH) {
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);

                        int datalen = ins.readInt();
                        byte data[] = new byte[datalen];
                        ins.readFully(data);

                        SubServTCPService.this.relay(channel, data);

                        bytesCount += channellen + datalen + 8;
                    } else if(type == TCPProvider.MESSAGE_TYPE_SUBSCRIBE) {
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);
                        synchronized(subscriptions) {
                            subscriptions.add(new SubscriptionRecord(new String(channel)));
                        }
                    } else if(type == TCPProvider.MESSAGE_TYPE_UNSUBSCRIBE) {
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);
                        String re = new String(channel);
                        synchronized(subscriptions) {
                            for(int i=0, n=subscriptions.size(); i<n; i++) {
                                if(subscriptions.get(i).regex.equals(re)) {
                                    subscriptions.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {

            }

            ///////////////////////
            // Something bad happened, close this connection.
            try {
                sock.close();
            } catch (IOException ex) {
            }

            synchronized(clients) {
                clients.remove(this);

            }
            synchronized (mCallback){
                mCallback.clientDisconnecting(clients.size());
            }
        }

        public void send(String chanstr, byte channel[], byte data[])
        {
            try {
                synchronized(subscriptions) {
                    for(SubscriptionRecord sr : subscriptions) {
                        if(sr.pat.matcher(chanstr).matches()) {
                            outs.writeInt(TCPProvider.MESSAGE_TYPE_PUBLISH);
                            outs.writeInt(channel.length);
                            outs.write(channel);
                            outs.writeInt(data.length);
                            outs.write(data);
                            outs.flush();
                            return;
                        }
                    }
                }
            } catch (IOException ex) {
            }
        }
    }

    public static void main(String args[])
    {
        try {
            int port = 7700;
            if (args.length > 0)
                port = Integer.parseInt(args[0]);
            new TCPService(port);
        } catch (IOException ex) {
            System.out.println("Ex: "+ex);
        }
    }
}
