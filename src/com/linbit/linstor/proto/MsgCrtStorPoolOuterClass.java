// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/MsgCrtStorPool.proto

package com.linbit.linstor.proto;

public final class MsgCrtStorPoolOuterClass {
  private MsgCrtStorPoolOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface MsgCrtStorPoolOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.linbit.linstor.proto.MsgCrtStorPool)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
     */
    boolean hasStorPool();
    /**
     * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
     */
    com.linbit.linstor.proto.StorPoolOuterClass.StorPool getStorPool();
    /**
     * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
     */
    com.linbit.linstor.proto.StorPoolOuterClass.StorPoolOrBuilder getStorPoolOrBuilder();
  }
  /**
   * <pre>
   * linstor - Create storage pool
   * </pre>
   *
   * Protobuf type {@code com.linbit.linstor.proto.MsgCrtStorPool}
   */
  public  static final class MsgCrtStorPool extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:com.linbit.linstor.proto.MsgCrtStorPool)
      MsgCrtStorPoolOrBuilder {
    // Use MsgCrtStorPool.newBuilder() to construct.
    private MsgCrtStorPool(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private MsgCrtStorPool() {
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private MsgCrtStorPool(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.linbit.linstor.proto.StorPoolOuterClass.StorPool.Builder subBuilder = null;
              if (((bitField0_ & 0x00000001) == 0x00000001)) {
                subBuilder = storPool_.toBuilder();
              }
              storPool_ = input.readMessage(com.linbit.linstor.proto.StorPoolOuterClass.StorPool.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(storPool_);
                storPool_ = subBuilder.buildPartial();
              }
              bitField0_ |= 0x00000001;
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
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.internal_static_com_linbit_linstor_proto_MsgCrtStorPool_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.internal_static_com_linbit_linstor_proto_MsgCrtStorPool_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool.class, com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool.Builder.class);
    }

    private int bitField0_;
    public static final int STOR_POOL_FIELD_NUMBER = 1;
    private com.linbit.linstor.proto.StorPoolOuterClass.StorPool storPool_;
    /**
     * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
     */
    public boolean hasStorPool() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
     */
    public com.linbit.linstor.proto.StorPoolOuterClass.StorPool getStorPool() {
      return storPool_ == null ? com.linbit.linstor.proto.StorPoolOuterClass.StorPool.getDefaultInstance() : storPool_;
    }
    /**
     * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
     */
    public com.linbit.linstor.proto.StorPoolOuterClass.StorPoolOrBuilder getStorPoolOrBuilder() {
      return storPool_ == null ? com.linbit.linstor.proto.StorPoolOuterClass.StorPool.getDefaultInstance() : storPool_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasStorPool()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!getStorPool().isInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeMessage(1, getStorPool());
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, getStorPool());
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool)) {
        return super.equals(obj);
      }
      com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool other = (com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool) obj;

      boolean result = true;
      result = result && (hasStorPool() == other.hasStorPool());
      if (hasStorPool()) {
        result = result && getStorPool()
            .equals(other.getStorPool());
      }
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasStorPool()) {
        hash = (37 * hash) + STOR_POOL_FIELD_NUMBER;
        hash = (53 * hash) + getStorPool().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parseFrom(
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
    public static Builder newBuilder(com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool prototype) {
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
     * linstor - Create storage pool
     * </pre>
     *
     * Protobuf type {@code com.linbit.linstor.proto.MsgCrtStorPool}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.linbit.linstor.proto.MsgCrtStorPool)
        com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPoolOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.internal_static_com_linbit_linstor_proto_MsgCrtStorPool_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.internal_static_com_linbit_linstor_proto_MsgCrtStorPool_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool.class, com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool.Builder.class);
      }

      // Construct using com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool.newBuilder()
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
          getStorPoolFieldBuilder();
        }
      }
      public Builder clear() {
        super.clear();
        if (storPoolBuilder_ == null) {
          storPool_ = null;
        } else {
          storPoolBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.internal_static_com_linbit_linstor_proto_MsgCrtStorPool_descriptor;
      }

      public com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool getDefaultInstanceForType() {
        return com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool.getDefaultInstance();
      }

      public com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool build() {
        com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool buildPartial() {
        com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool result = new com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        if (storPoolBuilder_ == null) {
          result.storPool_ = storPool_;
        } else {
          result.storPool_ = storPoolBuilder_.build();
        }
        result.bitField0_ = to_bitField0_;
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
        if (other instanceof com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool) {
          return mergeFrom((com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool other) {
        if (other == com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool.getDefaultInstance()) return this;
        if (other.hasStorPool()) {
          mergeStorPool(other.getStorPool());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        if (!hasStorPool()) {
          return false;
        }
        if (!getStorPool().isInitialized()) {
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private com.linbit.linstor.proto.StorPoolOuterClass.StorPool storPool_ = null;
      private com.google.protobuf.SingleFieldBuilderV3<
          com.linbit.linstor.proto.StorPoolOuterClass.StorPool, com.linbit.linstor.proto.StorPoolOuterClass.StorPool.Builder, com.linbit.linstor.proto.StorPoolOuterClass.StorPoolOrBuilder> storPoolBuilder_;
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      public boolean hasStorPool() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      public com.linbit.linstor.proto.StorPoolOuterClass.StorPool getStorPool() {
        if (storPoolBuilder_ == null) {
          return storPool_ == null ? com.linbit.linstor.proto.StorPoolOuterClass.StorPool.getDefaultInstance() : storPool_;
        } else {
          return storPoolBuilder_.getMessage();
        }
      }
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      public Builder setStorPool(com.linbit.linstor.proto.StorPoolOuterClass.StorPool value) {
        if (storPoolBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          storPool_ = value;
          onChanged();
        } else {
          storPoolBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      public Builder setStorPool(
          com.linbit.linstor.proto.StorPoolOuterClass.StorPool.Builder builderForValue) {
        if (storPoolBuilder_ == null) {
          storPool_ = builderForValue.build();
          onChanged();
        } else {
          storPoolBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      public Builder mergeStorPool(com.linbit.linstor.proto.StorPoolOuterClass.StorPool value) {
        if (storPoolBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001) &&
              storPool_ != null &&
              storPool_ != com.linbit.linstor.proto.StorPoolOuterClass.StorPool.getDefaultInstance()) {
            storPool_ =
              com.linbit.linstor.proto.StorPoolOuterClass.StorPool.newBuilder(storPool_).mergeFrom(value).buildPartial();
          } else {
            storPool_ = value;
          }
          onChanged();
        } else {
          storPoolBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      public Builder clearStorPool() {
        if (storPoolBuilder_ == null) {
          storPool_ = null;
          onChanged();
        } else {
          storPoolBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      public com.linbit.linstor.proto.StorPoolOuterClass.StorPool.Builder getStorPoolBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getStorPoolFieldBuilder().getBuilder();
      }
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      public com.linbit.linstor.proto.StorPoolOuterClass.StorPoolOrBuilder getStorPoolOrBuilder() {
        if (storPoolBuilder_ != null) {
          return storPoolBuilder_.getMessageOrBuilder();
        } else {
          return storPool_ == null ?
              com.linbit.linstor.proto.StorPoolOuterClass.StorPool.getDefaultInstance() : storPool_;
        }
      }
      /**
       * <code>required .com.linbit.linstor.proto.StorPool stor_pool = 1;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          com.linbit.linstor.proto.StorPoolOuterClass.StorPool, com.linbit.linstor.proto.StorPoolOuterClass.StorPool.Builder, com.linbit.linstor.proto.StorPoolOuterClass.StorPoolOrBuilder> 
          getStorPoolFieldBuilder() {
        if (storPoolBuilder_ == null) {
          storPoolBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              com.linbit.linstor.proto.StorPoolOuterClass.StorPool, com.linbit.linstor.proto.StorPoolOuterClass.StorPool.Builder, com.linbit.linstor.proto.StorPoolOuterClass.StorPoolOrBuilder>(
                  getStorPool(),
                  getParentForChildren(),
                  isClean());
          storPool_ = null;
        }
        return storPoolBuilder_;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:com.linbit.linstor.proto.MsgCrtStorPool)
    }

    // @@protoc_insertion_point(class_scope:com.linbit.linstor.proto.MsgCrtStorPool)
    private static final com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool();
    }

    public static com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<MsgCrtStorPool>
        PARSER = new com.google.protobuf.AbstractParser<MsgCrtStorPool>() {
      public MsgCrtStorPool parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new MsgCrtStorPool(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<MsgCrtStorPool> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MsgCrtStorPool> getParserForType() {
      return PARSER;
    }

    public com.linbit.linstor.proto.MsgCrtStorPoolOuterClass.MsgCrtStorPool getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_linbit_linstor_proto_MsgCrtStorPool_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_linbit_linstor_proto_MsgCrtStorPool_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\032proto/MsgCrtStorPool.proto\022\030com.linbit" +
      ".linstor.proto\032\024proto/StorPool.proto\"G\n\016" +
      "MsgCrtStorPool\0225\n\tstor_pool\030\001 \002(\0132\".com." +
      "linbit.linstor.proto.StorPoolP\000"
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
          com.linbit.linstor.proto.StorPoolOuterClass.getDescriptor(),
        }, assigner);
    internal_static_com_linbit_linstor_proto_MsgCrtStorPool_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_linbit_linstor_proto_MsgCrtStorPool_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_linbit_linstor_proto_MsgCrtStorPool_descriptor,
        new java.lang.String[] { "StorPool", });
    com.linbit.linstor.proto.StorPoolOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
