package org.jacorb.transaction;

/*
 *        JacORB transaction service - a free TS for JacORB
 *
 *   Copyright (C) 1999-2004 LogicLand group Alex Sinishin.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

public class Timer extends Thread{

    private boolean stoped;

    class Channel{
        Sleeper slp;
        int     time;

        Channel(Sleeper _slp, int _time){
            slp  = _slp;
            time = _time;
        }
    }

    private Channel[] channels;
    private int       top;

    Timer(int max_of_chan){
        stoped   = true;
        channels = new Channel [max_of_chan];
        for (int i = 0;i < channels.length;i++){
            channels[i] = null;
        }
        top = 0;
        start();
    }

    private int find_free(){
        for (int i = 0;i < channels.length;i++){
            if (channels[i] == null){
                return i;
            }
        }
        throw new org.omg.CORBA.INTERNAL();
    }

    void pause(){
        synchronized(channels){
            if (!stoped){
                stoped = true;
            }
        }
    }

    void go(){
        synchronized(channels){
            if (stoped){
                stoped = false;
                resume();
            }
        }
    }

    void add_channel(Sleeper slp, int time){
        if (time <= 0){
            throw new org.omg.CORBA.INTERNAL();
        }
        Channel ch = new Channel(slp, time);

        synchronized(channels){
            int ix = find_free();
            if (ix > top){
                throw new org.omg.CORBA.INTERNAL();
            }
            if (ix == top){
                top++;
            }
            channels[ix] = ch;
        }

        go();
    }

    private void destroy_channel(int ix){
        synchronized(channels){
            channels[ix] = null;
            if ((top - 1) == ix){
                top--;
            }
            if (top == 0){
                stoped = true;
            }
        }
    }

    void kill_channel(Sleeper slp){
        synchronized(channels){
            for (int i = 0;i < top;i++){
                if (channels[i] != null){
		  if (channels[i].slp == slp){
		      destroy_channel(i);
		      return;
		  }
                }
            }
        }
    }

    private void count(){
        int sz = top;
        for (int i = 0;i < sz;i++){
            if (channels[i] != null){
                if (channels[i].time != 0){
                    channels[i].time--;
                    if (channels[i].time == 0){
                        channels[i].slp.wakeup();
                    }
                }
            }
        }
    }

    public void run(){
        try {
            for (;;){
                if (stoped){
                    suspend();
                } else {
                    sleep(1000);
                    count();
                }
            }
        } catch(Throwable e){
            e.printStackTrace();
            System.exit(1);
        }
    }

}

