/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/max/EnhancedKeystore/android/otclient/src/main/aidl/fi/aalto/ssg/opentee/IOTConnectionInterface.aidl
 */
package fi.aalto.ssg.opentee;
public interface IOTConnectionInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements fi.aalto.ssg.opentee.IOTConnectionInterface
{
private static final java.lang.String DESCRIPTOR = "fi.aalto.ssg.opentee.IOTConnectionInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an fi.aalto.ssg.opentee.IOTConnectionInterface interface,
 * generating a proxy if needed.
 */
public static fi.aalto.ssg.opentee.IOTConnectionInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof fi.aalto.ssg.opentee.IOTConnectionInterface))) {
return ((fi.aalto.ssg.opentee.IOTConnectionInterface)iin);
}
return new fi.aalto.ssg.opentee.IOTConnectionInterface.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_basicTypes:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
long _arg1;
_arg1 = data.readLong();
boolean _arg2;
_arg2 = (0!=data.readInt());
float _arg3;
_arg3 = data.readFloat();
double _arg4;
_arg4 = data.readDouble();
java.lang.String _arg5;
_arg5 = data.readString();
this.basicTypes(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
return true;
}
case TRANSACTION_teecInitializeContext:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.teecInitializeContext(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_teecFinalizeContext:
{
data.enforceInterface(DESCRIPTOR);
this.teecFinalizeContext();
reply.writeNoException();
return true;
}
case TRANSACTION_teecRegisterSharedMemory:
{
data.enforceInterface(DESCRIPTOR);
fi.aalto.ssg.opentee.imps.OTSharedMemory _arg0;
if ((0!=data.readInt())) {
_arg0 = fi.aalto.ssg.opentee.imps.OTSharedMemory.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _result = this.teecRegisterSharedMemory(_arg0);
reply.writeNoException();
reply.writeInt(_result);
if ((_arg0!=null)) {
reply.writeInt(1);
_arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_teecReleaseSharedMemory:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.teecReleaseSharedMemory(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_teecOpenSessionWithoutOp:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.ParcelUuid _arg1;
if ((0!=data.readInt())) {
_arg1 = android.os.ParcelUuid.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int[] _arg4;
int _arg4_length = data.readInt();
if ((_arg4_length<0)) {
_arg4 = null;
}
else {
_arg4 = new int[_arg4_length];
}
int _result = this.teecOpenSessionWithoutOp(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
reply.writeInt(_result);
reply.writeIntArray(_arg4);
return true;
}
case TRANSACTION_teecOpenSession:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
android.os.ParcelUuid _arg1;
if ((0!=data.readInt())) {
_arg1 = android.os.ParcelUuid.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
byte[] _arg4;
_arg4 = data.createByteArray();
int[] _arg5;
int _arg5_length = data.readInt();
if ((_arg5_length<0)) {
_arg5 = null;
}
else {
_arg5 = new int[_arg5_length];
}
fi.aalto.ssg.opentee.ISyncOperation _arg6;
_arg6 = fi.aalto.ssg.opentee.ISyncOperation.Stub.asInterface(data.readStrongBinder());
int _arg7;
_arg7 = data.readInt();
int _result = this.teecOpenSession(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7);
reply.writeNoException();
reply.writeInt(_result);
reply.writeIntArray(_arg5);
return true;
}
case TRANSACTION_teecCloseSession:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.teecCloseSession(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_teecInvokeCommandWithoutOp:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int[] _arg2;
int _arg2_length = data.readInt();
if ((_arg2_length<0)) {
_arg2 = null;
}
else {
_arg2 = new int[_arg2_length];
}
int _result = this.teecInvokeCommandWithoutOp(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
reply.writeIntArray(_arg2);
return true;
}
case TRANSACTION_teecInvokeCommand:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
byte[] _arg2;
_arg2 = data.createByteArray();
int[] _arg3;
int _arg3_length = data.readInt();
if ((_arg3_length<0)) {
_arg3 = null;
}
else {
_arg3 = new int[_arg3_length];
}
fi.aalto.ssg.opentee.ISyncOperation _arg4;
_arg4 = fi.aalto.ssg.opentee.ISyncOperation.Stub.asInterface(data.readStrongBinder());
int _arg5;
_arg5 = data.readInt();
int _result = this.teecInvokeCommand(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
reply.writeInt(_result);
reply.writeIntArray(_arg3);
return true;
}
case TRANSACTION_teecRequestCancellation:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.teecRequestCancellation(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_otInstallTA:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
byte[] _arg1;
_arg1 = data.createByteArray();
this.otInstallTA(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements fi.aalto.ssg.opentee.IOTConnectionInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
@Override public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, java.lang.String aString) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(anInt);
_data.writeLong(aLong);
_data.writeInt(((aBoolean)?(1):(0)));
_data.writeFloat(aFloat);
_data.writeDouble(aDouble);
_data.writeString(aString);
mRemote.transact(Stub.TRANSACTION_basicTypes, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int teecInitializeContext(java.lang.String name) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(name);
mRemote.transact(Stub.TRANSACTION_teecInitializeContext, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void teecFinalizeContext() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_teecFinalizeContext, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int teecRegisterSharedMemory(fi.aalto.ssg.opentee.imps.OTSharedMemory sharedMemory) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((sharedMemory!=null)) {
_data.writeInt(1);
sharedMemory.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_teecRegisterSharedMemory, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
if ((0!=_reply.readInt())) {
sharedMemory.readFromParcel(_reply);
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void teecReleaseSharedMemory(int smId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(smId);
mRemote.transact(Stub.TRANSACTION_teecReleaseSharedMemory, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int teecOpenSessionWithoutOp(int sid, android.os.ParcelUuid parcelUuid, int connMethod, int connData, int[] retOrigin) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sid);
if ((parcelUuid!=null)) {
_data.writeInt(1);
parcelUuid.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(connMethod);
_data.writeInt(connData);
if ((retOrigin==null)) {
_data.writeInt(-1);
}
else {
_data.writeInt(retOrigin.length);
}
mRemote.transact(Stub.TRANSACTION_teecOpenSessionWithoutOp, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
_reply.readIntArray(retOrigin);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int teecOpenSession(int sid, android.os.ParcelUuid parcelUuid, int connMethod, int connData, byte[] teecOperation, int[] retOrigin, fi.aalto.ssg.opentee.ISyncOperation syncOperation, int opHashCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sid);
if ((parcelUuid!=null)) {
_data.writeInt(1);
parcelUuid.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(connMethod);
_data.writeInt(connData);
_data.writeByteArray(teecOperation);
if ((retOrigin==null)) {
_data.writeInt(-1);
}
else {
_data.writeInt(retOrigin.length);
}
_data.writeStrongBinder((((syncOperation!=null))?(syncOperation.asBinder()):(null)));
_data.writeInt(opHashCode);
mRemote.transact(Stub.TRANSACTION_teecOpenSession, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
_reply.readIntArray(retOrigin);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//int teecOpenSessionWithByteArrayWrapper(int sid, in ParcelUuid parcelUuid, int connMethod, int connData, inout ByteArrayWrapper teecOperation, out int[] retOrigin);

@Override public void teecCloseSession(int sid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sid);
mRemote.transact(Stub.TRANSACTION_teecCloseSession, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int teecInvokeCommandWithoutOp(int sid, int commandId, int[] returnOrigin) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sid);
_data.writeInt(commandId);
if ((returnOrigin==null)) {
_data.writeInt(-1);
}
else {
_data.writeInt(returnOrigin.length);
}
mRemote.transact(Stub.TRANSACTION_teecInvokeCommandWithoutOp, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
_reply.readIntArray(returnOrigin);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int teecInvokeCommand(int sid, int commandId, byte[] teecOperation, int[] returnOrigin, fi.aalto.ssg.opentee.ISyncOperation syncOperation, int opHashCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sid);
_data.writeInt(commandId);
_data.writeByteArray(teecOperation);
if ((returnOrigin==null)) {
_data.writeInt(-1);
}
else {
_data.writeInt(returnOrigin.length);
}
_data.writeStrongBinder((((syncOperation!=null))?(syncOperation.asBinder()):(null)));
_data.writeInt(opHashCode);
mRemote.transact(Stub.TRANSACTION_teecInvokeCommand, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
_reply.readIntArray(returnOrigin);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void teecRequestCancellation(int opId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(opId);
mRemote.transact(Stub.TRANSACTION_teecRequestCancellation, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void otInstallTA(java.lang.String taName, byte[] taInBytes) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(taName);
_data.writeByteArray(taInBytes);
mRemote.transact(Stub.TRANSACTION_otInstallTA, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_basicTypes = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_teecInitializeContext = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_teecFinalizeContext = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_teecRegisterSharedMemory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_teecReleaseSharedMemory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_teecOpenSessionWithoutOp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_teecOpenSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_teecCloseSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_teecInvokeCommandWithoutOp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_teecInvokeCommand = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_teecRequestCancellation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_otInstallTA = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, java.lang.String aString) throws android.os.RemoteException;
public int teecInitializeContext(java.lang.String name) throws android.os.RemoteException;
public void teecFinalizeContext() throws android.os.RemoteException;
public int teecRegisterSharedMemory(fi.aalto.ssg.opentee.imps.OTSharedMemory sharedMemory) throws android.os.RemoteException;
public void teecReleaseSharedMemory(int smId) throws android.os.RemoteException;
public int teecOpenSessionWithoutOp(int sid, android.os.ParcelUuid parcelUuid, int connMethod, int connData, int[] retOrigin) throws android.os.RemoteException;
public int teecOpenSession(int sid, android.os.ParcelUuid parcelUuid, int connMethod, int connData, byte[] teecOperation, int[] retOrigin, fi.aalto.ssg.opentee.ISyncOperation syncOperation, int opHashCode) throws android.os.RemoteException;
//int teecOpenSessionWithByteArrayWrapper(int sid, in ParcelUuid parcelUuid, int connMethod, int connData, inout ByteArrayWrapper teecOperation, out int[] retOrigin);

public void teecCloseSession(int sid) throws android.os.RemoteException;
public int teecInvokeCommandWithoutOp(int sid, int commandId, int[] returnOrigin) throws android.os.RemoteException;
public int teecInvokeCommand(int sid, int commandId, byte[] teecOperation, int[] returnOrigin, fi.aalto.ssg.opentee.ISyncOperation syncOperation, int opHashCode) throws android.os.RemoteException;
public void teecRequestCancellation(int opId) throws android.os.RemoteException;
public void otInstallTA(java.lang.String taName, byte[] taInBytes) throws android.os.RemoteException;
}
