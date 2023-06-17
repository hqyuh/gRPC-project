package com.hqh.client.service;

import com.google.protobuf.Descriptors;
import com.hqh.Author;
import com.hqh.Book;
import com.hqh.BookAuthorServiceGrpc;
import com.hqh.proto.TempDB;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BookAuthorClientService {
    @GrpcClient("grpc-hqh-service")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient;

    @GrpcClient("grpc-hqh-service")
    BookAuthorServiceGrpc.BookAuthorServiceStub asynchronousClient;

    public Map<Descriptors.FieldDescriptor, Object> getAuthor(int authorID) {
        Author authorRequest = Author.newBuilder().setAuthorId(authorID).build();
        Author authorResponse = synchronousClient.getAuthor(authorRequest);
        return authorResponse.getAllFields();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthor(int authorID) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Author authorRequest = Author.newBuilder().setAuthorId(authorID).build();

        List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();

        asynchronousClient.getBooksByAuthor(authorRequest, new StreamObserver<>() {
            @Override
            public void onNext(Book book) {
                System.out.println("HAHA");
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyList();
    }

    public Map<String, Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Map<String, Map<Descriptors.FieldDescriptor, Object>> response = new HashMap<>();
        StreamObserver<Book> responseObserver = asynchronousClient.getExpensiveBook(new StreamObserver<>() {
            @Override
            public void onNext(Book book) {
                response.put("ExpensiveBook", book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        TempDB.getBooksFromTempDB().forEach(responseObserver::onNext);
        responseObserver.onCompleted();
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyMap();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByGender(String gender) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        StreamObserver<Book> responseObserver = asynchronousClient.getBookByAuthorGender(new StreamObserver<>() {
            @Override
            public void onNext(Book book) {
                System.out.println("Client");
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        TempDB.getAuthorsFromTempDB()
                .stream()
                .filter(author -> author.getGender().equalsIgnoreCase(gender))
                .forEach(author -> {
                    Book book = Book.newBuilder().setAuthorId(author.getAuthorId()).build();
                    responseObserver.onNext(book);
                });
        responseObserver.onCompleted();
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyList();
    }
}
