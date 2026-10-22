import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LibraryManagementSystem {
    private static final Path DATA_FILE = Paths.get("library_data.txt");
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Book> books = new ArrayList<>();
    private static int nextId = 1;

    private static class Book {
        int id;
        String title;
        String author;
        String issuedTo;

        Book(int id, String title, String author, String issuedTo) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.issuedTo = issuedTo;
        }
    }

    public static void main(String[] args) {
        loadBooks();
        while (true) {
            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Add a book");
            System.out.println("2. Show all books");
            System.out.println("3. Search books");
            System.out.println("4. Issue a book");
            System.out.println("5. Return a book");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addBook(); break;
                case "2": showBooks(books); break;
                case "3": searchBooks(); break;
                case "4": issueBook(); break;
                case "5": returnBook(); break;
                case "0": System.out.println("Goodbye!"); return;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void addBook() {
        System.out.print("Book title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        if (title.isEmpty() || author.isEmpty()) {
            System.out.println("Title and author cannot be empty.");
            return;
        }
        books.add(new Book(nextId++, title, author, ""));
        saveBooks();
        System.out.println("Book added successfully.");
    }

    private static void showBooks(List<Book> list) {
        if (list.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.printf("\n%-5s %-30s %-22s %s%n", "ID", "TITLE", "AUTHOR", "STATUS");
        System.out.println("-".repeat(78));
        for (Book book : list) {
            String status = book.issuedTo.isEmpty() ? "Available" : "Issued to " + book.issuedTo;
            System.out.printf("%-5d %-30s %-22s %s%n", book.id, shorten(book.title, 30), shorten(book.author, 22), status);
        }
    }

    private static String shorten(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    private static Book findBook(int id) {
        for (Book book : books) if (book.id == id) return book;
        return null;
    }

    private static Integer readBookId() {
        System.out.print("Book ID: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid numeric ID.");
            return null;
        }
    }

    private static void issueBook() {
        Integer id = readBookId();
        if (id == null) return;
        Book book = findBook(id);
        if (book == null) { System.out.println("Book not found."); return; }
        if (!book.issuedTo.isEmpty()) { System.out.println("This book is already issued to " + book.issuedTo + "."); return; }
        System.out.print("Borrower's name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Borrower's name cannot be empty."); return; }
        book.issuedTo = name;
        saveBooks();
        System.out.println("Book issued successfully.");
    }

    private static void returnBook() {
        Integer id = readBookId();
        if (id == null) return;
        Book book = findBook(id);
        if (book == null) System.out.println("Book not found.");
        else if (book.issuedTo.isEmpty()) System.out.println("This book is already available.");
        else {
            book.issuedTo = "";
            saveBooks();
            System.out.println("Book returned successfully.");
        }
    }

    private static void searchBooks() {
        System.out.print("Search by title or author: ");
        String query = scanner.nextLine().trim().toLowerCase();
        List<Book> matches = new ArrayList<>();
        for (Book book : books) {
            if (book.title.toLowerCase().contains(query) || book.author.toLowerCase().contains(query)) matches.add(book);
        }
        showBooks(matches);
    }

    // A simple tab-separated text file keeps this project dependency-free.
    private static void loadBooks() {
        if (!Files.exists(DATA_FILE)) return;
        try {
            for (String line : Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8)) {
                String[] fields = line.split("\t", -1);
                if (fields.length == 4) {
                    int id = Integer.parseInt(fields[0]);
                    books.add(new Book(id, decode(fields[1]), decode(fields[2]), decode(fields[3])));
                    nextId = Math.max(nextId, id + 1);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Could not read saved data: " + e.getMessage());
        }
    }

    private static void saveBooks() {
        List<String> lines = new ArrayList<>();
        for (Book book : books) {
            lines.add(book.id + "\t" + encode(book.title) + "\t" + encode(book.author) + "\t" + encode(book.issuedTo));
        }
        try {
            Files.write(DATA_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        return value.replace("%", "%25").replace("\t", "%09").replace("\n", "%0A").replace("\r", "%0D");
    }

    private static String decode(String value) {
        return value.replace("%0D", "\r").replace("%0A", "\n").replace("%09", "\t").replace("%25", "%");
    }
}
