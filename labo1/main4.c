#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include <string.h>

pthread_barrier_t barrierAB;
pthread_barrier_t barrierCR;

// const int NTh = 2;
// const int NBlocks = 2; 


const int M = 8;//IMPORTANT: M deve essere multiplo di NBlocks!
const int N = 8;
const int P = 4;


float** A; //M*N //8*8
float** B; //N*P //8*4
float** R; //M*P //8*4
float** C; //P*M //4*8
float** FINAL_RES; //P*P //4*4

struct I_F{ //indice di inizio e fine del blocco da passare ad un thread
    int inizioAB;
    int fineAB;
    int inizioCR;
    int fineCR;
};

// le matrici di input A B C vengono riempite in automatico sequenzialmente 
// partendo da 1, da "sinistra verso destra" e "dall'alto verso il basso" 




void* PRODCAB(void* inizio_fine){
    struct I_F* arg = (struct I_F*)inizio_fine;

// parte R = A * B  
    for(int i = arg->inizioAB; i<arg->fineAB; i++){
        float* r = malloc(P * sizeof(float)); //una riga di R
        
        for(int j = 0; j< P; j++){
            float aux = 0;

            for(int k = 0; k < M; k++){
                aux +=  A[i][k] * B[k][j];
            }

            r[j] = aux;
        }

        R[i] = r;
        
    }

pthread_barrier_wait(&barrierAB);

// parte FINAL_RES = C * R
    for(int i = arg->inizioCR; i<arg->fineCR; i++){
        float* r = malloc(P * sizeof(float)); //una riga di FINAL_RES
        
        for(int j = 0; j< P; j++){
            float aux = 0;

            for(int k = 0; k < M; k++){
                aux +=  C[i][k] * R[k][j];
            }

            r[j] = aux;
        }

        FINAL_RES[i] = r;
        
    }


pthread_barrier_wait(&barrierCR);


}

///////////////////////////////MAIN///////////////////////////////////////
int main(int argc, char** argv){
    //parte init matrici
/********************************************************/
    A = malloc(M * sizeof(float*));
    for(int i = 0 ;i<M; i++){
        A[i] = malloc(N * sizeof(float));
    }
    int count = 1;
    for(int i = 0; i< M; i++){
        for(int j = 0; j<N ; j++){
            A[i][j] = count;
            count++;
        }
    }
/********************************************************/
    B = malloc(N * sizeof(float*));
    for(int i = 0 ;i<N; i++){
        B[i] = malloc(P * sizeof(float));
    }
    count = 1;
    for(int i = 0; i< N; i++){
        for(int j = 0; j<P ; j++){
            B[i][j] = count;
            count++;
        }
    }
/********************************************************/
    C = malloc(P * sizeof(float*));
    for(int i = 0 ;i<P; i++){
        C[i] = malloc(M * sizeof(float));
    }
    count = 1;
    for(int i = 0; i< P; i++){
        for(int j = 0; j<M ; j++){
            C[i][j] = count;
            count++;
        }
    }
/********************************************************/
/********************************************************/
/********************************************************/
    R = malloc(M * sizeof(float*));
    for(int i = 0 ;i<M; i++){
        R[i] = malloc(P * sizeof(float));
    }
/********************************************************/
    FINAL_RES = malloc(P * sizeof(float*));
    for(int i = 0 ;i<P; i++){
        FINAL_RES[i] = malloc(P * sizeof(float));
    }
/********************************************************/
    


if(argc != 4){
    printf("INPUT= PROGRAMMA NUMERO_THREAD(0 se sequenziale) NUMERO_BLOCCHI_A NUMERO_BLOCCHI_C\n");
    return EXIT_FAILURE;
}

int nThrds = atoi(argv[1]);
int nBlcksA = atoi(argv[2]);
int nBlcksC = atoi(argv[3]);



if(M % nBlcksA != 0){
    printf("numero blocchi di A deve essere 1, 2 ,4 o 8 !\n");
    return(EXIT_FAILURE);
}

if(P % nBlcksC != 0){
    printf("numero blocchi di C deve essere 1, 2 o 4 !\n");
    return(EXIT_FAILURE);
}



struct timeval stop, start;
gettimeofday(&start, NULL); //start timer




pthread_t workers[nThrds];

pthread_barrier_init(&barrierAB, NULL, nThrds + 1);
pthread_barrier_init(&barrierCR, NULL, nThrds + 1);


struct I_F blocks[nBlcksA >= nBlcksC ? nBlcksA : nBlcksC];

blocks[0].inizioAB = 0;
blocks[0].fineAB = M / nBlcksA;
blocks[0].inizioCR = 0;
blocks[0].fineCR = P/nBlcksC;

if(nBlcksA >= nBlcksC){
    for(int i = 1; i < nBlcksA; i++){
        blocks[i].inizioAB = blocks[i-1].fineAB;
        blocks[i].fineAB = blocks[i-1].fineAB + M / nBlcksA;
        
        if(blocks[i-1].fineCR != P && blocks[i-1].fineCR != 0){
            blocks[i].inizioCR = blocks[i-1].fineCR;
            blocks[i].fineCR = blocks[i-1].fineCR + P / nBlcksC;
        }else{
            blocks[i].inizioCR = 0;
            blocks[i].fineCR = 0;
        }
    }
}else{
    for(int i = 1; i < nBlcksC; i++){
        blocks[i].inizioCR = blocks[i-1].fineCR;
        blocks[i].fineCR = blocks[i-1].fineCR + P / nBlcksC;
        
        if(blocks[i-1].fineAB != M && blocks[i-1].fineAB != 0){
            blocks[i].inizioAB = blocks[i-1].fineAB;
            blocks[i].fineAB = blocks[i-1].fineAB + M / nBlcksA;
        }else{
            blocks[i].inizioAB = 0;
            blocks[i].fineAB = 0;
        }
    }
}

//blocks testati OK







//metti a posto questione input num thread, togli NTh
//crea array con le struct che dai in pasto a thread, ciclo for per
// creazione thread i con passaggio argomento i di array delle struct I_F (i valori della I_F i_f
// cambieranno in base al num blocks assegnato da user == argv[2])


    struct I_F i_f = {0, 4, 0, 2};
    struct I_F i_f2 = {4, 8, 2, 4};

    pthread_create(&workers[0], NULL, PRODCAB, &i_f);
    pthread_create(&workers[1], NULL, PRODCAB, &i_f2);


    pthread_barrier_wait(&barrierAB);
    pthread_barrier_wait(&barrierCR);


                        for(int i = 0; i<M; i++){
                            for(int j = 0; j< P; j++){
                                printf("%f ", R[i][j]);
                            }
                            printf("\n");
                        }


                        printf("\n");
                        printf("\n");
                        printf("\n");


                        for(int i = 0; i<P; i++){
                            for(int j = 0; j< P; j++){
                                printf("%.1f ", FINAL_RES[i][j]);
                            }
                            printf("\n");
                        }




    for(int i = 0; i< 2; i++){
        pthread_join(workers[i], NULL);
    }

    pthread_barrier_destroy(&barrierAB);
    pthread_barrier_destroy(&barrierCR);





    gettimeofday(&stop, NULL);
    printf("took %lu us\n", (stop.tv_sec - start.tv_sec) * 1000000 + stop.tv_usec - start.tv_usec); 



}
