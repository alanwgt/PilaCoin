package com.alanwgt.helpers;

import java.io.*;
import java.net.DatagramPacket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ObjectUtil {

    static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(object);
        oos.flush();
        oos.close();

        return baos.toByteArray();
    }

    static Object deserialize(DatagramPacket packet) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }

    static Object deserialize(byte[] serialized) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serialized, 0, serialized.length);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }

    static byte[] hash(Object object) throws NoSuchAlgorithmException, IOException {
        return hash(serialize(object));
    }

    static byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
//        messageDigest.update(data);
        return messageDigest.digest(data);
    }
}
