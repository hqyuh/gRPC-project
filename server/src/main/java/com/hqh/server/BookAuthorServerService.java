package com.hqh.server;

import com.hqh.Author;
import com.hqh.Book;
import com.hqh.BookAuthorServiceGrpc;
import com.hqh.proto.TempDB;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class BookAuthorServerService extends BookAuthorServiceGrpc.BookAuthorServiceImplBase {
    @Override
    public void getAuthor(Author request, StreamObserver<Author> responseObserver) {
        TempDB.getAuthorsFromTempDB()
                .stream()
                .filter(author -> author.getAuthorId() == request.getAuthorId())
                .findFirst()
                .ifPresent(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getBooksByAuthor(Author request, StreamObserver<Book> responseObserver) {
        TempDB.getBooksFromTempDB()
                .stream()
                .filter(book -> book.getAuthorId() == request.getAuthorId())
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Book> getExpensiveBook(StreamObserver<Book> responseObserver) {
        return new StreamObserver<>() {
            Book expensiveBook = null;
            float priceTrack = 0;

            @Override
            public void onNext(Book book) {
                System.out.println("HAHA");
                if (book.getPrice() > priceTrack) {
                    priceTrack = book.getPrice();
                    expensiveBook = book;
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(expensiveBook);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Book> getBookByAuthorGender(StreamObserver<Book> responseObserver) {
        return new StreamObserver<>() {
            final List<Book> books = new ArrayList<>();

            @Override
            public void onNext(Book book) {
                System.out.println("Server");
                TempDB.getBooksFromTempDB()
                        .stream()
                        .filter(booksFromDB -> book.getAuthorId() == booksFromDB.getAuthorId())
                        .forEach(books::add);
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                books.forEach(responseObserver::onNext);
                responseObserver.onCompleted();
            }
        };
    }
}
