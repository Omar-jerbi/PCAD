#include<stdlib.h>
#include<stdio.h>

const int M = 4;
const int N = 4;
const int P = 2;

// le matrici in input vengono riempite in automatico sequenzialmente 
// partendo da 1, da "sinistra verso destra" e "dall'alto verso il basso" 




float* AUX(float* riga, int numCol,  float** b, int rB, int cB){
    float* res = malloc(cB * sizeof(float));
    
    for(int i = 0; i< cB; i++){
        float aux = 0;
        for(int k = 0; k < numCol; k++){
            aux += b[k][i] * riga[k];
        }
    res[i] = aux;
    }

    return res;
}


float** PROD(float** a, int rA, int cA, float** b, int rB, int cB){
    float** res = malloc(rA * sizeof(float*));
    for(int i = 0 ;i<rA; i++){
        res[i] = malloc(cB * sizeof(float));
    }

    
    for(int i = 0; i<rA; i++){
       res[i] = AUX(a[i],cA, b, rB, cB);
    }

    return res;
};

/////////////////////////////////////////////////////////////////
int main(){

    float** A = malloc(M * sizeof(float*));
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

    float** B = malloc(N * sizeof(float*));
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

    float** C = malloc(P * sizeof(float*));
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

    float** R = PROD(A, M, N, B, N, P);
    float** res = PROD(C, P, M, R, M, P);


    for(int i = 0; i<M; i++){
        for(int j = 0; j< P; j++){
            printf("%f ", R[i][j]);
        }
        printf("\n");
    }


printf("\n");
printf("\n");

    for(int i = 0; i<P; i++){
        for(int j = 0; j< P; j++){
            printf("%f ", res[i][j]);
        }
        printf("\n");
    }


}