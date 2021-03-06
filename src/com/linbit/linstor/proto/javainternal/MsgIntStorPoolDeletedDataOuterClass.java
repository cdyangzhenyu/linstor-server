// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/javainternal/MsgIntStorPoolDeletedData.proto

package com.linbit.linstor.proto.javainternal;

public final class MsgIntStorPoolDeletedDataOuterClass {
  private MsgIntStorPoolDeletedDataOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface MsgIntStorPoolDeletedDataOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedData)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * Storage pool name
     * </pre>
     *
     * <code>string stor_pool_name = 1;</code>
     */
    java.lang.String getStorPoolName();
    /**
     * <pre>
     * Storage pool name
     * </pre>
     *
     * <code>string stor_pool_name = 1;</code>
     */
    com.google.protobuf.ByteString
        getStorPoolNameBytes();
  }
  /**
   * <pre>
   * linstor - Internal message containing the identifier of a deleted but requested storage pool.
   * This message is basically just to make sure the satellite deletes the storage pool.
   * </pre>
   *
   * Protobuf type {@code com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedData}
   */
  public  static final class MsgIntStorPoolDeletedData extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedData)
      MsgIntStorPoolDeletedDataOrBuilder {
    // Use MsgIntStorPoolDeletedData.newBuilder() to construct.
    private MsgIntStorPoolDeletedData(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private MsgIntStorPoolDeletedData() {
      storPoolName_ = "";
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }
    private MsgIntStorPoolDeletedData(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!input.skipField(tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              java.lang.String s = input.readStringRequireUtf8();

              storPoolName_ = s;
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData.class, com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData.Builder.class);
    }

    public static final int STOR_POOL_NAME_FIELD_NUMBER = 1;
    private volatile java.lang.Object storPoolName_;
    /**
     * <pre>
     * Storage pool name
     * </pre>
     *
     * <code>string stor_pool_name = 1;</code>
     */
    public java.lang.String getStorPoolName() {
      java.lang.Object ref = storPoolName_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        storPoolName_ = s;
        return s;
      }
    }
    /**
     * <pre>
     * Storage pool name
     * </pre>
     *
     * <code>string stor_pool_name = 1;</code>
     */
    public com.google.protobuf.ByteString
        getStorPoolNameBytes() {
      java.lang.Object ref = storPoolName_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        storPoolName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (!getStorPoolNameBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, storPoolName_);
      }
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (!getStorPoolNameBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, storPoolName_);
      }
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData)) {
        return super.equals(obj);
      }
      com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData other = (com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData) obj;

      boolean result = true;
      result = result && getStorPoolName()
          .equals(other.getStorPoolName());
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + STOR_POOL_NAME_FIELD_NUMBER;
      hash = (53 * hash) + getStorPoolName().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * <pre>
     * linstor - Internal message containing the identifier of a deleted but requested storage pool.
     * This message is basically just to make sure the satellite deletes the storage pool.
     * </pre>
     *
     * Protobuf type {@code com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedData}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedData)
        com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedDataOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData.class, com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData.Builder.class);
      }

      // Construct using com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        storPoolName_ = "";

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_descriptor;
      }

      public com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData getDefaultInstanceForType() {
        return com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData.getDefaultInstance();
      }

      public com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData build() {
        com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData buildPartial() {
        com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData result = new com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData(this);
        result.storPoolName_ = storPoolName_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData) {
          return mergeFrom((com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData other) {
        if (other == com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData.getDefaultInstance()) return this;
        if (!other.getStorPoolName().isEmpty()) {
          storPoolName_ = other.storPoolName_;
          onChanged();
        }
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private java.lang.Object storPoolName_ = "";
      /**
       * <pre>
       * Storage pool name
       * </pre>
       *
       * <code>string stor_pool_name = 1;</code>
       */
      public java.lang.String getStorPoolName() {
        java.lang.Object ref = storPoolName_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          storPoolName_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <pre>
       * Storage pool name
       * </pre>
       *
       * <code>string stor_pool_name = 1;</code>
       */
      public com.google.protobuf.ByteString
          getStorPoolNameBytes() {
        java.lang.Object ref = storPoolName_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          storPoolName_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <pre>
       * Storage pool name
       * </pre>
       *
       * <code>string stor_pool_name = 1;</code>
       */
      public Builder setStorPoolName(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        storPoolName_ = value;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * Storage pool name
       * </pre>
       *
       * <code>string stor_pool_name = 1;</code>
       */
      public Builder clearStorPoolName() {
        
        storPoolName_ = getDefaultInstance().getStorPoolName();
        onChanged();
        return this;
      }
      /**
       * <pre>
       * Storage pool name
       * </pre>
       *
       * <code>string stor_pool_name = 1;</code>
       */
      public Builder setStorPoolNameBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        storPoolName_ = value;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }


      // @@protoc_insertion_point(builder_scope:com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedData)
    }

    // @@protoc_insertion_point(class_scope:com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedData)
    private static final com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData();
    }

    public static com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<MsgIntStorPoolDeletedData>
        PARSER = new com.google.protobuf.AbstractParser<MsgIntStorPoolDeletedData>() {
      public MsgIntStorPoolDeletedData parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new MsgIntStorPoolDeletedData(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<MsgIntStorPoolDeletedData> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MsgIntStorPoolDeletedData> getParserForType() {
      return PARSER;
    }

    public com.linbit.linstor.proto.javainternal.MsgIntStorPoolDeletedDataOuterClass.MsgIntStorPoolDeletedData getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n2proto/javainternal/MsgIntStorPoolDelet" +
      "edData.proto\022%com.linbit.linstor.proto.j" +
      "avainternal\"3\n\031MsgIntStorPoolDeletedData" +
      "\022\026\n\016stor_pool_name\030\001 \001(\tb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_linbit_linstor_proto_javainternal_MsgIntStorPoolDeletedData_descriptor,
        new java.lang.String[] { "StorPoolName", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
