/**
 * junixsocket
 *
 * Copyright 2009-2019 Christian Kohlschütter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.newsclub.net.unix.rmi;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;

/**
 * A specialized subclass of {@link RemoteFileDescriptorBase}, specifically for
 * {@link FileOutputStream}s.
 * 
 * @author Christian Kohlschütter
 */
public final class RemoteFileOutput extends RemoteFileDescriptorBase<FileOutputStream> implements
    Closeable {
  private static final long serialVersionUID = 1L;

  /**
   * Creates an uninitialized instance; used for externalization.
   * 
   * @see #readExternal(ObjectInput)
   */
  public RemoteFileOutput() {
    super();
  }

  /**
   * Creates a new RemoteFileOut instance, encapsulating a FileOutputStream so that it can be shared
   * with other processes via RMI.
   * 
   * @param socketFactory The socket factory.
   * @param fout The FileOutputStream.
   * @throws IOException if the operation fails.
   */
  public RemoteFileOutput(AFUNIXRMISocketFactory socketFactory, FileOutputStream fout)
      throws IOException {
    super(socketFactory, fout, fout.getFD(), RemoteFileDescriptorBase.MAGIC_VALUE_MASK
        | RemoteFileDescriptorBase.BIT_WRITABLE);
  }

  /**
   * Returns a FileOutputStream for the given instance. This either is the actual instance provided
   * by the constructor or a new instance created from the file descriptor.
   * 
   * @return The FileOutputStream.
   * @throws IOException if the operation fails.
   */
  public synchronized FileOutputStream asFileOutputStream() throws IOException {
    if (resource != null) {
      return resource;
    }
    if ((getMagicValue() & RemoteFileDescriptorBase.BIT_WRITABLE) == 0) {
      throw new IOException("FileDescriptor is not writable");
    }
    return (this.resource = new FileOutputStream(getFileDescriptor()) {
      @Override
      public synchronized void close() throws IOException {
        RemoteFileOutput.this.close();
        super.close();
      }
    });
  }
}