package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Service extends Remote {

  int put(String key, String value) throws RemoteException;

  int post(String key, String value) throws RemoteException;

  String get(String key) throws RemoteException;

  int delete(String key) throws RemoteException;

  int getMapSize() throws RemoteException;
}
