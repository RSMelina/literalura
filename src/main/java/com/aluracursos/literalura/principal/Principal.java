package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repositorio.AutorRepository;
import com.aluracursos.literalura.repositorio.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private List<Autor> autores;
    private List<Libro> libros;
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }


    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Búsqueda de libro por título
                    2 - Lista de todos los libros buscados 
                    3 - Lista de autores buscados
                    4 - Listar autores vivos en determinado año
                    5 - Listar libros por idioma
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    mostrarLibrosBuscados();
                    break;
                case 3:
                    mostrarAutoresBuscados();
                    break;
                case 4:
                    mostrarAutoresVivos();
                    break;
                case 5:
                    mostrarLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    private Datos getDatosLibros(){
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro =teclado.nextLine();
        var json =consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        return datosBusqueda;
    }

    private Libro agregarLibroDB(DatosLibros datosLibros, Autor autor){
        Libro libro = new Libro(datosLibros, autor);
        return libroRepository.save(libro);
    }

    private void buscarLibroPorTitulo() {
        Datos datos = getDatosLibros();

        if (!datos.resultados().isEmpty()){
            DatosLibros datosLibros = datos.resultados().get(0);
            DatosAutor datosAutor = datosLibros.autor().get(0);
            Libro librpBuscado= libroRepository.findByTituloIgnoreCase(datosLibros.titulo());

            if(librpBuscado != null){
                System.out.println(librpBuscado);
                System.out.println("El libro ya fue registrado");
            }else{
                Autor autorBuscado= autorRepository.findByNombreIgnoreCase(datosAutor.nombre());

                if (autorBuscado == null){
                    Autor autor = new Autor(datosAutor);
                    autorRepository.save(autor);
                    Libro libro = agregarLibroDB(datosLibros, autor);
                    System.out.println(libro);
                }else{
                    Libro libro = agregarLibroDB(datosLibros, autorBuscado);
                    System.out.println(libro);
                }
            }
        }else{
            System.out.println("libro no encontrado");
        }
    }

    private void mostrarLibrosBuscados(){
        libros= libroRepository.findAll();
        if(!libros.isEmpty()){
            libros.stream().forEach(System.out::println);
        }else{
            System.out.println("No se encuentra registrado el libro.");
        }
    }

    private void mostrarAutoresBuscados(){
        autores = autorRepository.findAll();
        if (!autores.isEmpty()){
            autores.stream().forEach(System.out::println);
        }else{
            System.out.println("No se encuentra registrado el autor");
        }
    }

    private void mostrarAutoresVivos(){
        System.out.println("Para concoer los autores vivos de determinado año, ingrese el año");
        String fecha = teclado.nextLine();
        try {
            List<Autor> autoresVivosEnCiertoAnio= autorRepository.autorVivoEnDeterminadoAnio(fecha);
            if(!autoresVivosEnCiertoAnio.isEmpty()){
                autoresVivosEnCiertoAnio.stream().forEach(System.out::println);
            }else {
                System.out.println("No existen Autores vivos en ese año");
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void mostrarLibrosPorIdioma(){
        System.out.println("""
                1- Español (ES)
                2-Inglés (EN)
                
                3- Regresar al menú principal
                
                Por favor, ingrese el número de opción para elegir el idioma de lo slibros a consultar:
                """);

        int opcion;
        opcion = teclado.nextInt();
        teclado.nextLine();
        switch (opcion){
            case 1:
                libros = libroRepository.findByIdiomasContaining("es");
                if(!libros.isEmpty()){
                    libros.stream().forEach(System.out::println);
                }else{
                    System.out.println("El libro no esta disponible en Español");
                }
                break;
            case 2:
                libros=libroRepository.findByIdiomasContaining("en");
                if(!libros.isEmpty()){
                    libros.stream().forEach(System.out::println);
                }else {
                    System.out.println("El libro no esta disponible en Inglés");
                }
                break;
            case 3:
                muestraElMenu();
                break;
            default:
                System.out.println("La opción seleccionada no es valida");
        }
    }

}


